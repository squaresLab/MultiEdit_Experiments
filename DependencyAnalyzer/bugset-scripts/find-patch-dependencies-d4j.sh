#!/bin/bash

# Must be run from bugset-scripts/; don't run from anywhere else; there are hard path dependencies in the script.
# Usage: ./find-patch-dependencies-d4j.sh -p d4j-project -b d4j-bug-number -d d4j-output-bug-directory
#
#  E.g: ./find-patch-dependencies-d4j.sh -p Math -b 31 -d /here/is/where/I/want/to/dump/my/d4j/bugs/

# Here are the hard path dependencies; I don't want to parameterize these, so they're hard coded
DEPENDENCY_ANALYZER_JAR=$(realpath "../jar/DependencyAnalyzer.jar")
ANALYZE_SINGLE_CLASS_SHSCRIPT=$(realpath "analyze-single-class-d4j.sh")
GET_CHANGED_LINES_PYSCRIPT=$(realpath "get-changed-lines.py")

#don't add a slash at the end of this directory
#the deletion operations at the end of the scripts relies on this fact
ANALYSIS_OUTPUT_SUBDIR='dep-analysis-output'

f_analyze_output_format="-Oe -Om"
function analyze {
    f_analyze_bugwd=$1
    f_analyze_target_classname=$2
    f_analyze_lines=$3

    printf 'Analyzing %s in %s\n' $f_analyze_target_classname ${f_analyze_bugwd##*/} # ##*/ gets the innermost directory name

    #make output directory
    f_analyze_output_dir=${f_analyze_bugwd}/$ANALYSIS_OUTPUT_SUBDIR/
    mkdir -p $f_analyze_output_dir

    #don't do anything further if there are no lines to analyze
    if [[ -z $f_analyze_lines ]] || [[ $f_analyze_lines =~ ^\ +$ ]]; then
        return 0
    fi

    #define output files
    f_analyze_outfile_Dc=$f_analyze_output_dir/$f_analyze_target_classname'.Dc'
    f_analyze_outfile_Df=$f_analyze_output_dir/$f_analyze_target_classname'.Df'
    f_analyze_outfile_Da=$f_analyze_output_dir/$f_analyze_target_classname'.Da'
    f_analyze_outfile_Do=$f_analyze_output_dir/$f_analyze_target_classname'.Do'

    #define logging files
    f_analyze_log_Dc=$f_analyze_outfile_Dc'.log'
    f_analyze_log_Df=$f_analyze_outfile_Df'.log'
    f_analyze_log_Da=$f_analyze_outfile_Da'.log'
    f_analyze_log_Do=$f_analyze_outfile_Do'.log'

    #collect arguments together
    f_analyze_args=" -a $DEPENDENCY_ANALYZER_JAR -b $f_analyze_bugwd -t $f_analyze_target_classname \
                    $f_analyze_output_format -lines $f_analyze_lines "
    f_analyze_args_Dc="$f_analyze_args -Dc -o $f_analyze_outfile_Dc"
    f_analyze_args_Df="$f_analyze_args -Df -o $f_analyze_outfile_Df"
    f_analyze_args_Da="$f_analyze_args -Da -o $f_analyze_outfile_Da"
    f_analyze_args_Do="$f_analyze_args -Do -o $f_analyze_outfile_Do"

    #run each analysis
    bash $ANALYZE_SINGLE_CLASS_SHSCRIPT $f_analyze_args_Dc >& $f_analyze_log_Dc
    bash $ANALYZE_SINGLE_CLASS_SHSCRIPT $f_analyze_args_Df >& $f_analyze_log_Df
    bash $ANALYZE_SINGLE_CLASS_SHSCRIPT $f_analyze_args_Da >& $f_analyze_log_Da
    bash $ANALYZE_SINGLE_CLASS_SHSCRIPT $f_analyze_args_Do >& $f_analyze_log_Do
}

D4J_PROJECT=
D4J_BUGNUM=
D4J_WD=

while (( "$#" )); do
    case "$1" in
        -p|--d4j-project)
            D4J_PROJECT=$2
            shift 2
            ;;
        -b|--d4j-bug-number)
            D4J_BUGNUM=$2
            shift 2
            ;;
        -d|--d4j-output-bug-directory) #wherever to check out D4J bug samples to, must already exist
            D4J_WD=$2
            shift 2
            ;;
        --) #end of parsing
            shift 1
            break
            ;;
        *) #anything else
            echo "Ignoring unexpected argument $1"
            shift 1
            ;;
    esac
done

if [[ -z $D4J_PROJECT ]] || [[ -z $D4J_BUGNUM ]] || [[ -z $D4J_WD ]]; then
    echo "Usage: ./find-patch-dependencies-d4j.sh -p d4j-project -b d4j-bug-number -d d4j-output-bug-directory"
    echo
    echo "  E.g: ./find-patch-dependencies-d4j.sh -p Math -b 31 -d /here/is/where/I/want/to/dump/my/d4j/bugs"
    echo " This script must be run from bugset-scripts/; don't run from anywhere else; there are hard path dependencies in the script."
    exit 1
fi

#convert inputted path to absolute path
D4J_WD=$(realpath $D4J_WD)

D4J_VERSION_ID_BUGGY=${D4J_BUGNUM}'b'
D4J_VERSION_ID_FIXED=${D4J_BUGNUM}'f'
#don't add a slash at the end of these working directories
#the deletion operations at the end of the scripts relies on this fact
WD_BUGGY=${D4J_WD}/${D4J_PROJECT}${D4J_VERSION_ID_BUGGY}
WD_FIXED=${D4J_WD}/${D4J_PROJECT}${D4J_VERSION_ID_FIXED}

defects4j checkout -p $D4J_PROJECT -v $D4J_VERSION_ID_BUGGY -w $WD_BUGGY
defects4j checkout -p $D4J_PROJECT -v $D4J_VERSION_ID_FIXED -w $WD_FIXED

old_starting_dir=$PWD
cd $WD_BUGGY
D4J_SRC_DIR_BUGGY=$(defects4j export -p dir.src.classes)
cd $WD_FIXED
D4J_SRC_DIR_FIXED=$(defects4j export -p dir.src.classes)
cd $old_starting_dir

D4J_SRC_DIR=
if [[ $D4J_SRC_DIR_BUGGY != $D4J_SRC_DIR_FIXED ]]; then
    echo "ERROR: dir.src.classes is different between buggy and fixed versions."
    exit 42
else
    D4J_SRC_DIR=$D4J_SRC_DIR_BUGGY
fi

CHANGED_LINES_RAWOUT=$(python3 $GET_CHANGED_LINES_PYSCRIPT $WD_BUGGY $WD_FIXED $D4J_SRC_DIR)

#iterate over lines in $CHANGED_LINES_RAWOUT
i=0
class_name=
buggy_lines=
fixed_lines=
while IFS= read -r line; do
    if   [[ $i -eq 0 ]]; then
        class_name=$line
    elif [[ $i -eq 1 ]]; then
        buggy_lines=${line#*Buggy: } #strip the "Buggy: " prefix
        analyze $WD_BUGGY $class_name "$buggy_lines"
    else
        fixed_lines=${line#*Fixed: } #Strip the "Fixed: " prefix
        analyze $WD_FIXED $class_name "$fixed_lines"
    fi
    let i++
    let i=i%3
done <<< "$CHANGED_LINES_RAWOUT"

#remove non-analysis output files to clean up space
find $WD_BUGGY -not -samefile $WD_BUGGY -not -wholename "$WD_BUGGY/$ANALYSIS_OUTPUT_SUBDIR*" -delete
find $WD_FIXED -not -samefile $WD_FIXED -not -wholename "$WD_FIXED/$ANALYSIS_OUTPUT_SUBDIR*" -delete