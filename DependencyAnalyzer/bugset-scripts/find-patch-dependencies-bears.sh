#!/bin/bash

# Usage ./find-patch-dependencies-bears.sh -B bears-benchmark-dir -b bug-id -d bears-output-bug-directory
#  E.g: ./find-patch-dependencies-bears.sh -B here/is/bears-benchmark/ -b Bears-1 -d here/is/where/I/want/to/dump/my/bears/bugs

# Here are the hard path dependencies; I don't want to parameterize these, so they're hard coded
DEPENDENCY_ANALYZER_JAR=$(realpath "../jar/DependencyAnalyzer.jar")
ANALYZE_SINGLE_CLASS_SHSCRIPT=$(realpath "analyze-single-class-bears.sh")
GET_CHANGED_LINES_PYSCRIPT=$(realpath "helpers/get-changed-lines.py")

BEARS_BENCHMARK=
BUGID=
BEARS_WORKPLACE=
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
            BEARS_WORKPLACE=$2
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

if [[ -z $BEARS_BENCHMARK ]] || [[ -z $BUGID ]] || [[ -z $BEARS_WORKPLACE ]]; then
    echo "Usage ./find-patch-dependencies-bears.sh -B bears-benchmark-dir -b bug-id -d bears-output-bug-directory"
    echo
    echo " E.g: ./find-patch-dependencies-bears.sh -B here/is/bears-benchmark/ -b Bears-1 -d here/is/where/I/want/to/dump/my/bears/bugs"
    exit 1
fi

#BEARS_WORKPLACE must be an absolute path; the Bears checkout scripts don't mix well with relative paths
BEARS_WORKPLACE=$(realpath $BEARS_WORKPLACE)

BEARS_WORKPLACE_BUGGY=$BEARS_WORKPLACE/'Buggy'
mkdir -p $BEARS_WORKPLACE_BUGGY
BEARS_WORKPLACE_FIXED=$BEARS_WORKPLACE/'Fixed'
mkdir -p $BEARS_WORKPLACE_FIXED
