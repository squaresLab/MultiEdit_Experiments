#!/bin/bash

PROJECT="$1"
NUMBER="$2"
WORKINGDIR="$3"

mkdir -p "$WORKINGDIR"
defects4j checkout -p "$PROJECT" -v "$NUMBER"b -w "$WORKINGDIR"/buggy
defects4j checkout -p "$PROJECT" -v "$NUMBER"f -w "$WORKINGDIR"/patched


cd "$WORKINGDIR"/buggy || exit
defects4j compile

defects4j export -p tests.trigger > neg.tests
defects4j export -p tests.relevant > rel.tests
MODIFIED=$(defects4j export -p classes.modified)
MODIFIED=$(echo $MODIFIED | sed 's/ /:/g')

BUGGYSOURCE=$(defects4j export -p dir.src.classes)
TARGETCLASSB=$(defects4j export -p dir.bin.classes)
TARGETTESTB=$(defects4j export -p dir.bin.tests)
COMPILECPB=$(defects4j export -p cp.compile)
TESTCPB=$(defects4j export -p cp.test)


cd ../patched || exit
defects4j compile
PATCHEDSOURCE=$(defects4j export -p dir.src.classes)
TARGETCLASSP=$(defects4j export -p dir.bin.classes)
TARGETTESTP=$(defects4j export -p dir.bin.tests)
defects4j export -p cp.compile
COMPILECPP=$(defects4j export -p cp.compile)
echo "running export test"
defects4j export -p cp.test
echo "running export test again"
TESTCPP=$(defects4j export -p cp.test)
defects4j compile # this is purely for some mockito bugs where the test classes disappear?


cd ..

FILE=defects4j.config
/bin/cat <<EOM >$FILE
buggyClassFolder=$WORKINGDIR/buggy/$TARGETCLASSB
buggyTestFolder=$WORKINGDIR/buggy/$TARGETTESTB
patchedClassFolder=$WORKINGDIR/patched/$TARGETCLASSP
patchedTestFolder=$WORKINGDIR/patched/$TARGETTESTP
relevantTests=$WORKINGDIR/buggy/rel.tests
negativeTests=$WORKINGDIR/buggy/neg.tests
buggyTestClassPath=$TESTCPB
buggySrcClassPath=$COMPILECPB
patchedTestClassPath=$TESTCPP
patchedSrcClassPath=$COMPILECPP
modifiedClasses=$MODIFIED
buggySource=$WORKINGDIR/buggy/$BUGGYSOURCE
patchedSource=$WORKINGDIR/patched/$PATCHEDSOURCE
EOM

