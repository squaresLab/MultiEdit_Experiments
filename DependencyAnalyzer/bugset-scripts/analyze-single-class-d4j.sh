#!/bin/bash

# Usage: ./analyze-single-class-d4j.sh
#            -a,--analyzer-jar-path <arg>        Path to DependencyAnalyzer.jar
#            -b,--bug-working-directory <arg>    Path to wherever you exported your D4J bug
#            -t,--target <arg>                   Target class to analyze.
#            -o,--output <arg>                   File to write output to. Default is to print to stdout.
#            -Da,--find-anti-dependencies
#            -Dc,--find-control-dependencies
#            -Df,--find-flow-dependencies
#            -Do,--find-output-dependencies
#            -Oe,--output-dependency-existence
#            -Om,--output-dependency-map
#            -lines,--lines-to-analyze <arg...>

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
if [[ -z $D4J_HOME ]]; then
  echo "D4J_HOME is not set!"
  exit 1
fi

ANALYZER=
BUGWD=
ANALYSIS_OPT_TARGET=
OUTPUT_PATH=

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
    -b|--bug-working-directory) #wherever you exported your d4j bug
      parse_lines=0
      BUGWD=$2
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
      fi
      shift 1
      ;;
  esac
done

#Convert inputted paths to absolute paths
ANALYZER=$(realpath $ANALYZER)
BUGWD=$(realpath $BUGWD)
OUTPUT_PATH=$(realpath $OUTPUT_PATH)

ANALYSIS_OPT_OUTPUT="" #default value is to have no -o option
if [[ -n OUTPUT_PATH ]]; then
  #if OUTPUT_PATH is not an empty string, then set a non-default output option
  ANALYSIS_OPT_OUTPUT="-o $OUTPUT_PATH"
fi

old_starting_dir=$PWD

cd $BUGWD


ANALYSIS_OPT_TCP=$(defects4j export -p dir.bin.classes)

if ! [[ -e $ANALYSIS_OPT_TCP ]]; then
    defects4j compile
fi

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

cd $old_starting_dir
