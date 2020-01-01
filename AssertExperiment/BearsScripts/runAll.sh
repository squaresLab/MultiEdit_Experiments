#!/bin/bash

sh obtaindiff.sh $1 $2 $3 $4
rm -rf $3/"$1""$2"/bugorig
rm -rf $3/"$1""$2"/fix

mkdir $3/"$1""$2"/results

#export D4J_HOME=~/genprog/defects4j
#export MULTIEDIT_HOME=~/MultiEdit_Experiments
#export GP4J_HOME=$MULTIEIT_HOME/AssertExperiment/genprog4java

mkdir $3/"$1""$2"/zemp

sh setup.sh $BEARSPATH $3/"$1""$2"/zemp Bears-$2 /usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java $MULTIEDIT_HOME/AssertExperiment/genprog4java ~/.m2/repository/ 0 > $3/"$1""$2"/results/orig.out

#sh runGenProgForBug.sh

#LOWERCASEPACKAGE=`echo $1 | tr '[:upper:]' '[:lower:]'`

mv $3/"$1""$2"/zemp $3/"$1""$2"/bugorig

rm -rf $3/"$1""$2"/bugorig/Bears-$2/tmp
rm -rf $3/"$1""$2"/bugorig/Bears-$2/$5

java -cp .:$GP4J_HOME/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar ParseScript $3/"$1""$2" $GP4J_HOME $3/"$1""$2"/zemp/ $MULTIEDIT_HOME/AssertExperiment/results/"$1""$2".out Bears-$2

