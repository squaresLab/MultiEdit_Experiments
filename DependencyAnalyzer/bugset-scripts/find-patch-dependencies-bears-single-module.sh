#!/bin/bash

# Usage ./find-patch-dependencies-bears-single-module.sh -B bears-benchmark-dir -b bug-id -d bears-output-bug-directory
#  E.g: ./find-patch-dependencies-bears-single-module.sh -B here/is/bears-benchmark/ -b Bears-1 -d here/is/where/I/want/to/dump/my/bears/bugs

# Here are the hard path dependencies; I don't want to parameterize these, so they're hard coded
DEPENDENCY_ANALYZER_JAR=$(realpath "../jar/DependencyAnalyzer.jar")
ANALYZE_SINGLE_CLASS_SHSCRIPT=$(realpath "analyze-single-class-bears.sh")
GET_CHANGED_LINES_PYSCRIPT=$(realpath "helpers/get-changed-lines.py")
LOCAL_CHECKOUT_FIX_PYSCRIPT=$(realpath "helpers/bears/checkout_fix.py")

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