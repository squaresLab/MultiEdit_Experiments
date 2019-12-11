#!/bin/bash

PROJECT="$1"
NUMBER="$2"
WORKINGDIR="$3"

mkdir -p "$WORKINGDIR"
defects4j checkout -p "$PROJECT" -v "$NUMBER"b -w "$WORKINGDIR"
cd "$WORKINGDIR" || exit
defects4j compile

defects4j export -p tests.trigger > neg.tests
defects4j export -p tests.relevant > all.tests

TESTWD=$(defects4j export -p dir.src.tests)
TARGETCLASS=$(defects4j export -p dir.bin.classes)
TARGETTEST=$(defects4j export -p dir.bin.tests)
COMPILECP=$(defects4j export -p cp.compile)
TESTCP=$(defects4j export -p cp.test)

FILE=defects4j.config
/bin/cat <<EOM >$FILE
classFolder=$WORKINGDIR/$TARGETCLASS
testFolder=$WORKINGDIR/$TARGETTEST
relevantTests=$WORKINGDIR/all.tests
negativeTests=$WORKINGDIR/neg.tests
testClassPath=$TESTCP
srcClassPath=$COMPILECP
EOM