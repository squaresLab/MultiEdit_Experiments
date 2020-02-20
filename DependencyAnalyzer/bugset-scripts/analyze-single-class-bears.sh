#!/bin/bash

# Must be run from bugset-scripts/; don't run from anywhere else; there are hard path dependencies in the script.
# Executes dependency analysis on a single Java class in D4J with provided line numbers

# Usage: ./analyze-single-class-d4j.sh
#            -a,--analyzer-jar-path <arg>        Path to DependencyAnalyzer.jar
#            -w,--workplace <arg>                Path to wherever Bears bugs are stored
#            -b,--bug-id <arg>                   Bug ID (e.g: Bears-1)
#            -t,--target <arg>                   Target class to analyze.
#            -o,--output <arg>                   File to write output to. Default is to print to stdout.
#            -Da,--find-anti-dependencies
#            -Dc,--find-control-dependencies
#            -Df,--find-flow-dependencies
#            -Do,--find-output-dependencies
#            -Oe,--output-dependency-existence
#            -Om,--output-dependency-map
#            -lines,--lines-to-analyze <arg...>

COMPILE_BEARS_BUG_PYSCRIPT=$(realpath "helpers/bears/compile_bears_bug.py")

ANALYZER=
BEARS_WORKPLACE=
BUGID=
ANALYSIS_OPT_TARGET=
OUTPUT_PATH=""

#set default values (absence of flag) for analysis flags
ANALYSIS_OPT_Da=""
ANALYSIS_OPT_Dc=""
ANALYSIS_OPT_Df=""
ANALYSIS_OPT_Do=""
ANALYSIS_OPT_Oe=""
ANALYSIS_OPT_Om=""
#set default value (no -lines option) for -lines
ANALYSIS_OPT_LINES=""

parse_lines=0
#bash args parsing: https://medium.com/@Drew_Stokes/bash-argument-parsing-54f3b81a6a8f
while (( "$#" )); do
  case "$1" in
    -a|--analyzer-jar-path)
      parse_lines=0
      ANALYZER=$2
      shift 2
      ;;
    -w|--workplace)
      parse_lines=0
      BEARS_WORKPLACE=$2
      shift 2
      ;;
    -b|--bug-id)
      parse_lines=0
      BUGID=$2
      shift 2
      ;;
    -t|--target)
      parse_lines=0
      ANALYSIS_OPT_TARGET=$2
      shift 2
      ;;
    -o|--output)
      parse_lines=0
      OUTPUT_PATH=$2
      shift 2
      ;;
    -Da|--find-anti-dependencies)
      parse_lines=0
      ANALYSIS_OPT_Da=$1
      shift 1
      ;;
    -Dc|--find-control-dependencies)
      parse_lines=0
      ANALYSIS_OPT_Dc=$1
      shift 1
      ;;
    -Df|--find-flow-dependencies)
      parse_lines=0
      ANALYSIS_OPT_Df=$1
      shift 1
      ;;
    -Do|--find-output-dependencies)
      parse_lines=0
      ANALYSIS_OPT_Do=$1
      shift 1
      ;;
    -Oe|--output-dependency-existence)
      parse_lines=0
      ANALYSIS_OPT_Oe=$1
      shift 1
      ;;
    -Om|--output-dependency-map)
      parse_lines=0
      ANALYSIS_OPT_Om=$1
      shift 1
      ;;
    -lines|--lines-to-analyze)
      parse_lines=1
      ANALYSIS_OPT_LINES+="-lines "
      shift 1
      ;;
    --) #end of parsing
      shift 1
      break
      ;;
    *) #anything else
      if [[ $parse_lines ]]; then
        ANALYSIS_OPT_LINES+="$1 "
      else
        echo "Ignoring unexpected argument $1"
      fi
      shift 1
      ;;
  esac
done

if [[ -z $ANALYZER ]]; then
    echo "Missing required parameter -a"
    exit 1
fi
if [[ -z $BEARS_WORKPLACE ]]; then
    echo "Missing required parameter -w"
    exit 1
fi
if [[ -z $BUGID ]]; then
    echo "Missing required parameter -b"
    exit 1
fi
if [[ -z $ANALYSIS_OPT_TARGET ]]; then
    echo "Missing required parameter -t"
    exit 1
fi

#Convert inputted paths to absolute paths
ANALYZER=$(realpath $ANALYZER)
BEARS_WORKPLACE=$(realpath $BEARS_WORKPLACE)
if [[ -n $OUTPUT_PATH ]]; then
    OUTPUT_PATH=$(realpath -m $OUTPUT_PATH)
fi

ANALYSIS_OPT_OUTPUT="" #default value is to have no -o option
if [[ -n $OUTPUT_PATH ]]; then
  #if OUTPUT_PATH is not an empty string, then set a non-default output option
  ANALYSIS_OPT_OUTPUT="-o $OUTPUT_PATH"
fi

TARGET_DIR=$BEARS_WORKPLACE/$BUGID/'target'/

python3 $COMPILE_BEARS_BUG_PYSCRIPT --bugId $BUGID --workspace $BEARS_WORKPLACE

#add dependencies of project
DEPENDENCIES_DIR=$TARGET_DIR/'dependency'
DEPENDENCIES=$(find $DEPENDENCIES_DIR -not -samefile $DEPENDENCIES_DIR)
ANALYSIS_OPT_TCP=
while IFS= read -r line; do
    ANALYSIS_OPT_TCP=$ANALYSIS_OPT_TCP:$line
done <<< "$DEPENDENCIES"

#Add target/classes, the default place to put .class files of application code
ANALYSIS_OPT_TCP=$ANALYSIS_OPT_TCP:$TARGET_DIR/'classes'

java -jar $ANALYZER \
  -t $ANALYSIS_OPT_TARGET \
  -tcp $ANALYSIS_OPT_TCP \
  $ANALYSIS_OPT_Da \
  $ANALYSIS_OPT_Dc \
  $ANALYSIS_OPT_Df \
  $ANALYSIS_OPT_Do \
  $ANALYSIS_OPT_OUTPUT \
  $ANALYSIS_OPT_Oe \
  $ANALYSIS_OPT_Om \
  $ANALYSIS_OPT_LINES