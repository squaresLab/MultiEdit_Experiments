#!/bin/bash

#This script is called by prepareBug.sh
#The purpose of this script is to set up the environment to run Genprog of a particular defects4j bug.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

#Output
#Creates a config file

# Example usage, VM:
#./createConfigFile.sh 

if [ "$#" -ne 10 ]; then
    echo "This script should be run with 11 parameters:"
	echo "1st param: LOWERCASEPACKAGE, sentence case (ex: lang, chart, closure, math, time)"
	echo "2nd param: BUGNUMBER (ex: 1,2,3,4,...)"
	echo "3rd param: BUGSFOLDER is the folder where the bug files will be cloned to. Starting from $D4J_HOME"
	echo "4th param: APPROACH is the repair approach to use (e.g., gp, trp, par, all)"
	echo "5th param: DIROFJAVA7 is the folder where the java 7 instalation is located"
	echo "6th param: SRCFOLDER"
	echo "7th param: CONFIGLIBS"
	echo "8th param: WD"
	echo "9th param: TESTCP"
	echo "10th param: COMPILECP"
	exit 0
fi


LOWERCASEPACKAGE="$1"
BUGNUMBER="$2"
BUGSFOLDER="$3"
APPROACH="$4"
DIROFJAVA7="$5"
SRCFOLDER="$6"
CONFIGLIBS="$7"
WD="$8"
TESTCP="$9"
COMPILECP="${10}"
BUGWD=$D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/


if [ "$APPROACH" = "gp" ]; then
	#Create config file 
FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = no
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
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

fakeJunitDir = /home/ylyu1/Fake-JUnit
GP4J_HOME = /home/ylyu1/genprog/genprog4java/
skipFailedSanity = true


EOM

fi

if [ "$APPROACH" = "trp" ]; then
#Create config file 
FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = no
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
#class or method
testGranularity=class

# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0

# edits for PAR, GenProg, TrpAutoRepair
edits=APPEND;DELETE;REPLACE

# optionally you can provide a probabilistic model to modify the distribution it uses to pick the mutation operators
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt

# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1

# trp for TrpAutoRepair, gp for GenProg and PAR 
search=trp

# used only for TrpAutoRepair. value=400
maxVariants=400


EOM

fi

if [ "$APPROACH" = "par" ]; then
#Create config file 
FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = no
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
#class or method
testGranularity=class

# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0 

# edits for PAR, GenProg, TrpAutoRepair
edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT

# optionally you can provide a probabilistic model to modify the distribution it uses to pick the mutation operators
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt

# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1

# trp for TrpAutoRepair, gp for GenProg and PAR 
search=gp

EOM

fi

#Here we basically maintain the params of genprog as the default ones and add all possible mutation operations

if [ "$APPROACH" = "all" ]; then
#Create config file 
FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = no
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
#class or method
testGranularity=class

# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=0.1 

# edits for PAR, GenProg, TrpAutoRepair
edits=APPEND;DELETE;REPLACE;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT

# optionally you can provide a probabilistic model to modify the distribution it uses to pick the mutation operators
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt

# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=0.65
positivePathWeight=0.35

# trp for TrpAutoRepair, gp for GenProg and PAR 
search=gp

fakeJunitDir = /home/ylyu1/Fake-JUnit
GP4J_HOME = /home/ylyu1/genprog/genprog4java/
skipFailedSanity = true

EOM

fi
