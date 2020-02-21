#!/bin/bash

# Usage ./find-patch-dependencies-bears-single-module.sh -B bears-benchmark-dir -b bug-id -d bears-output-bug-directory
#  E.g: ./find-patch-dependencies-bears-single-module.sh -B here/is/bears-benchmark/ -b Bears-1 -d here/is/where/I/want/to/dump/my/bears/bugs

# Here are the hard path dependencies; I don't want to parameterize these, so they're hard coded
DEPENDENCY_ANALYZER_JAR=$(realpath "../jar/DependencyAnalyzer.jar")
ANALYZE_SINGLE_CLASS_SHSCRIPT=$(realpath "analyze-single-class-bears.sh")
GET_CHANGED_LINES_PYSCRIPT=$(realpath "helpers/get-changed-lines.py")
LOCAL_CHECKOUT_FIX_PYSCRIPT=$(realpath "helpers/bears/checkout_fix.py")

#don't add a slash at the end of this directory
#the deletion operations at the end of the scripts relies on this fact
ANALYSIS_OUTPUT_SUBDIR='dep-analysis-output'

f_analyze_output_format="-Oe -Om"
function analyze {
    f_analyze_bugwd=$1
    f_analyze_target_classname=$2
    f_analyze_lines=$3
    f_analyze_bears_workspace=$4 #buggy or fixed workspace
    f_analyze_bugid=$5

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
    f_analyze_args=" -a $DEPENDENCY_ANALYZER_JAR -w $f_analyze_bears_workspace -b $f_analyze_bugid \
                    -t $f_analyze_target_classname $f_analyze_output_format -lines $f_analyze_lines "
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

BEARS_BENCHMARK=
BUGID=
BEARS_WORKSPACE=
while (( "$#" )); do
    case "$1" in
        -B|--bears-benchmark) #wherever is the master branch of bears-benchmark/
            BEARS_BENCHMARK=$2
            shift 2
            ;;
        -b|--bears-bug-id)
            BUGID=$2
            shift 2
            ;;
        -d|--bears-output-bug-directory) #wherever to check out Bears bug samples to, must already exist
            BEARS_WORKSPACE=$2
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

if [[ -z $BEARS_BENCHMARK ]] || [[ -z $BUGID ]] || [[ -z $BEARS_WORKSPACE ]]; then
    echo "Usage ./find-patch-dependencies-bears-single-module.sh -B bears-benchmark-dir -b bug-id -d bears-output-bug-directory"
    echo
    echo " E.g: ./find-patch-dependencies-bears-single-module.sh -B here/is/bears-benchmark/ -b Bears-1 -d here/is/where/I/want/to/dump/my/bears/bugs"
    exit 1
fi

#BEARS_WORKPLACE must be an absolute path; the Bears checkout scripts don't mix well with relative paths
BEARS_WORKSPACE=$(realpath $BEARS_WORKSPACE)

BEARS_WORKSPACE_BUGGY=$BEARS_WORKSPACE/'Buggy'
mkdir -p $BEARS_WORKSPACE_BUGGY
BEARS_WORKSPACE_FIXED=$BEARS_WORKSPACE/'Fixed'
mkdir -p $BEARS_WORKSPACE_FIXED

CHECKOUT_BUG_PYSCRIPT=$BEARS_BENCHMARK/'scripts'/'checkout_bug.py'
python2.7 $CHECKOUT_BUG_PYSCRIPT --bugId $BUGID --workspace $BEARS_WORKSPACE_BUGGY
CHECKOUT_FIX_PYSCRIPT=$BEARS_BENCHMARK/'scripts'/'checkout_fix.py'
if ! [[ -e $CHECKOUT_FIX_PYSCRIPT ]]; then
    cp $LOCAL_CHECKOUT_FIX_PYSCRIPT $CHECKOUT_FIX_PYSCRIPT
fi
python2.7 $CHECKOUT_FIX_PYSCRIPT --bugId $BUGID --workspace $BEARS_WORKSPACE_FIXED

WD_BUGGY=$BEARS_WORKSPACE_BUGGY/$BUGID
WD_FIXED=$BEARS_WORKSPACE_FIXED/$BUGID

BUGNUM=$(printf $BUGID | grep -Eo '[0-9]+$')
SRC_DIR_RELATIVE=
if [[ BUGNUM -ge 98 ]] && [[ BUGNUM -le 139 ]]; then
    #traccar puts source code directly under src/
    SRC_DIR_RELATIVE='src'/
else
    #almost everyone else uses the standard src/main/java/
    SRC_DIR_RELATIVE='src'/'main'/'java'
fi

CHANGED_LINES_RAWOUT=$(python3 $GET_CHANGED_LINES_PYSCRIPT $WD_BUGGY $WD_FIXED $SRC_DIR_RELATIVE)

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
        analyze $WD_BUGGY $class_name "$buggy_lines" $BEARS_WORKSPACE_BUGGY $BUGID
    else
        fixed_lines=${line#*Fixed: } #Strip the "Fixed: " prefix
        analyze $WD_FIXED $class_name "$fixed_lines" $BEARS_WORKSPACE_FIXED $BUGID
    fi
    let i++
    let i=i%3
done <<< "$CHANGED_LINES_RAWOUT"

#remove non-analysis output files to clean up space
find $WD_BUGGY -not -samefile $WD_BUGGY -not -wholename "$WD_BUGGY/$ANALYSIS_OUTPUT_SUBDIR*" -delete
find $WD_FIXED -not -samefile $WD_FIXED -not -wholename "$WD_FIXED/$ANALYSIS_OUTPUT_SUBDIR*" -delete