#!/bin/bash


# Usage: ./find-patch-dependencies-d4j.sh -p d4j-project -b d4j-bug-number -d d4j-output-bug-directory
#
#  E.g: ./find-patch-dependencies-d4j.sh -p Math -b 31 -d /here/is/where/I/want/to/dump/my/d4j/bugs/

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
    exit 1
fi

#convert inputted path to absolute path
D4J_WD=$(realpath $D4J_WD)

D4J_VERSION_ID_BUGGY=${D4J_BUGNUM}'b'
D4J_VERSION_ID_FIXED=${D4J_BUGNUM}'f'
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

CHANGED_LINES_RAWOUT=$(python3 get-changed-lines.py $WD_BUGGY $WD_FIXED $D4J_SRC_DIR)

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
    else
        fixed_lines=${line#*Fixed: } #Strip the "Fixed: " prefix
        echo $class_name
        echo $buggy_lines
        echo $fixed_lines
    fi
    let i++
    let i=i%3
done <<< "$CHANGED_LINES_RAWOUT"