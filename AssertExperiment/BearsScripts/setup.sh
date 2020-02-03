#!/bin/bash

#Location of BEARS project
BEARSPATH=$1
#Location you want bug to be put in
BUGPATH=$2
#BugID to be checked out
BUGID=$3
#Dir of java
JAVADIR=$4
#GP4J home
GP4J_HOME=$5
#maven repo
MAVENREPO=$6
#GP4J seed
SEED=$7

python $BEARSPATH/scripts/checkout_bug.py --bugId $BUGID --workspace $BUGPATH

python $BEARSPATH/scripts/compile_bug.py --bugId $BUGID --workspace $BUGPATH

cp $8 $9

java FixClassPath $BUGPATH/$BUGID/classpath.info $MAVENREPO

java TestParser $BUGPATH/$BUGID/repairnator.maven.testproject.log $BUGPATH/$BUGID

#java TestNameFaultLocalizer $BUGPATH/$BUGID

java AllClassFinder $BUGPATH/$BUGID/src/main/java $BUGPATH/$BUGID/allclasses.txt

#read LINEE < $BUGPATH/$BUGID/negtestfaultloc.txt

read LINE < $BUGPATH/$BUGID/classpath.infoa

echo $GP4J_HOME

bash createConfig.sh $JAVADIR $BUGPATH/$BUGID $BUGPATH/$BUGID/allclasses.txt $GP4J_HOME $LINE $SEED

FILE2=$BUGPATH/$BUGID/runCompile.sh
/bin/cat <<EOM >$FILE2
python $BEARSPATH/scripts/compile_bug.py --bugId $BUGID --workspace $BUGPATH
EOM

cd $BUGPATH/$BUGID

timeout -sHUP 4h $JAVADIR -ea -Dlog4j.configurationFile=file:"$GP4J_HOME"/src/log4j.properties -Dfile.encoding=UTF-8 -classpath "$GP4J_HOME"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar clegoues.genprog4java.main.Main $BUGPATH/$BUGID/bears.config | tee $BUGPATH/$BUGID/logSeed$SEED.txt



