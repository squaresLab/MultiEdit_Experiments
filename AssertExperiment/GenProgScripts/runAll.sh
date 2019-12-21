#!/bin/bash

sh obtaindiff.sh $1 $2 $3 $4
rm -rf $3/"$1""$2"/bugorig
rm -rf $3/"$1""$2"/fix

mkdir $3/"$1""$2"/results

#export D4J_HOME=~/genprog/defects4j
#export MULTIEDIT_HOME=~/MultiEdit_Experiments
#export GP4J_HOME=$MULTIEIT_HOME/AssertExperiment/genprog4java

sh runGenProgForBug.sh $1 $2 allHuman 20 zemp gp 0 0 false /usr/lib/jvm/java-1.8.0-openjdk-amd64 /usr/lib/jvm/java-1.8.0-openjdk-amd64 false . false . > $3/"$1""$2"/results/orig.out

#sh runGenProgForBug.sh

LOWERCASEPACKAGE=`echo $1 | tr '[:upper:]' '[:lower:]'`

mv $D4J_HOME/zemp/"$LOWERCASEPACKAGE""$2"Buggy $3/"$1""$2"/bugorig

rm -rf $3/"$1""$2"/bugorig/tmp
rm -rf $3/"$1""$2"/bugorig/$5

java -cp .:$GP4J_HOME/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar ParseScript $3/"$1""$2" $GP4J_HOME $D4J_HOME/zemp/"$LOWERCASEPACKAGE""$2"Buggy "sh "$MULTIEDIT_HOME"/AssertExperiment/GenProgScripts/runGenProgForBug.sh ""$1"" ""$2"" allHuman 20 zemp gp 0 0 false /usr/lib/jvm/java-1.8.0-openjdk-amd64 /usr/lib/jvm/java-1.8.0-openjdk-amd64 false . false ."


