#!/bin/bash

#This script is called by prepareBug.sh
#The purpose of this script is to set up the environment to run Genprog of a particular defects4j bug.


#Output
#Creates a config file

# Example usage, VM:
#./createConfigFile.sh 

if [ "$#" -ne 6 ]; then
    echo "This script should be run with 5 parameters:"
	exit 0
fi



DIROFJAVA7="$1"
FOLDERPATH="$2"
TARGETCLASSNAME="$3"
GP4J_HOME="$4"
FULLCLASSPATH="$5"
SEED="$6"

#Here we basically maintain the params of genprog as the default ones and add all possible mutation operations


#Create config file 
FILE=$FOLDERPATH/bears.config
/bin/cat <<EOM >$FILE
seed = $SEED
skipFailedSanity=true
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7
workingDir = $FOLDERPATH
outputDir = $FOLDERPATH/tmp
classSourceFolder = $FOLDERPATH/target/classes
classTestFolder = $FOLDERPATH/target/test-classes
libs = $GP4J_HOME/lib/junittestrunner.jar
sourceDir = /src/main/java
positiveTests = $FOLDERPATH/pos.tests
negativeTests = $FOLDERPATH/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$FOLDERPATH/target/classes:$FOLDERPATH/target/test-classes:$FULLCLASSPATH
srcClassPath=$FOLDERPATH/target/classes:$FULLCLASSPATH
compileCommand = bash $FOLDERPATH/runCompile.sh
targetClassName = $TARGETCLASSNAME
#class or method
testGranularity=class
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=0.1 
# edits for PAR, GenProg, TrpAutoRepair
edits=APPEND;DELETE;REPLACE
# optionally you can provide a probabilistic model to modify the distribution it uses to pick the mutation operators
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=0.65
positivePathWeight=0.35
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=gp
fakeJunitDir = $MULTIEDIT_HOME/AssertExperiment/Fake-JUnit/
GP4J_HOME = $MULTIEDIT_HOME/AssertExperiment/genprog4java/
skipFailedSanity = true
EOM

