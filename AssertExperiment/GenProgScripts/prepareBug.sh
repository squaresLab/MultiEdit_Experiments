#!/bin/bash

#This script is called by runGenProgForBug.sh
#The purpose of this script is to set up the environment to run Genprog of a particular defects4j bug.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on each of the folders indicated. 

# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3th param: testing option (ex: humanMade, generated)
# 4th param: test suite sample size (ex: 1, 100)
# 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)
# 6th param is the folder where the java 7 instalation is located
# 7th param is the folder where the java 8 instalation is located

# Example usage, VM:
#./prepareBug.sh Math 2 allHuman 100 ExamplesCheckedOut gp /usr/lib/jvm/java-7-oracle/ /usr/lib/jvm/java-8-oracle/ true <path to neg.test> true <path to pos.test>

if [ "$#" -ne 12 ]; then
    echo "This script should be run with 12 parameters:"
	echo "1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param: bug number (ex: 1,2,3,4,...)"
	echo "3th param: testing option (ex: humanMade, generated)"
	echo "4th param: test suite sample size (ex: 1, 100)"
	echo "5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME"
	echo "6th param is the repair approach to use (e.g., gp, trp, par, all)"
	echo "7th param is the folder where the java 7 instalation is located"
	echo "8th param is the folder where the java 8 instalation is located"
	echo "9th param is set to \"true\" if negative tests are to be specified using sampled tests else set this to \"false\""
	echo "10th param is the path to file containing sampled negative tests"
	echo "11th param is set to \"true\" if positive tests are to be specified using sampled tests else set this to \"false\""
	echo "12th param is the path to file containing sampled positive tests"
    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
OPTION="$3"
TESTSUITESAMPLE="$4"
BUGSFOLDER="$5"
APPROACH="$6"
DIROFJAVA7="$7"
DIROFJAVA8="$8"
SAMPLENEGTESTS="$9"
NEGTESTPATH="${10}"
SAMPLEPOSTESTS="${11}"
POSTESTPATH="${12}"

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/

currentDir=$(pwd)

#copy these files to the source control

mkdir -p $D4J_HOME/$BUGSFOLDER

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy and fixed versions of the code (latter to make second testsuite
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

##defects4j checkout -p $1 -v "$BUGNUMBER"f -w $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy
do
    pushd $D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done
# Common genprog libs: junit test runner and the like
CONFIGLIBS=$GP4J_HOME"/lib/junittestrunner.jar"
#:"$GP4J_HOME"/lib/commons-io-1.4.jar:"$GP4J_HOME"/lib/junit-4.12.jar:"$GP4J_HOME"/lib/hamcrest-core-1.3.jar"

cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
TESTWD=`defects4j export -p dir.src.tests`
SRCFOLDER=`defects4j export -p dir.bin.classes`
COMPILECP=`defects4j export -p cp.compile`
TESTCP=`defects4j export -p cp.test`
WD=`defects4j export -p dir.src.classes`
cd $BUGWD/$WD

#Create file to run defects4j compile

FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=$DIROFJAVA7
export PATH=$DIROFJAVA7/bin/:$PATH
#sudo update-java-alternatives -s java-7-oracle
cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
$D4J_HOME/framework/bin/defects4j compile
if [[ \$? -ne 0 ]]; then
      echo "error compiling defect"
      exit 1
fi
export JAVA_HOME=$DIROFJAVA8
export PATH=$DIROFJAVA8/bin/:$PATH
#sudo update-java-alternatives -s java-8-oracle
EOM

chmod 777 $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh

cd $currentDir

./createConfigFile.sh $LOWERCASEPACKAGE $BUGNUMBER $BUGSFOLDER $APPROACH $DIROFJAVA7 $SRCFOLDER $CONFIGLIBS $WD $TESTCP $COMPILECP

cd $BUGWD

#  get passing and failing tests as well as files
#info about the bug

if [ "$SAMPLENEGTESTS" = "true" ]; then
        if [ "$NEGTESTPATH" = "" ]; then
                echo "please enter path to file containing negative test cases"
                exit 1
        fi
        cp $NEGTESTPATH $BUGWD/neg.tests
else	
	defects4j export -p tests.trigger > $BUGWD/neg.tests
fi

currentpid="$currentpid $!"
wait $currentpid

case "$OPTION" in
"humanMade" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH $BUGWD/pos.tests
	else
	     	defects4j export -p tests.all > $BUGWD/pos.tests
	fi
;;
"allHuman" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH  $BUGWD/pos.tests
	else
		defects4j export -p tests.all > $BUGWD/pos.tests
	fi
;;
"onlyRelevant" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH  $BUGWD/pos.tests
	else
		defects4j export -p tests.relevant > $BUGWD/pos.tests
        fi
;;
esac

currentpid="$currentpid $!"
wait $currentpid

#Remove a percentage of the positive tests in the test suite
cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITESAMPLE -ne 100 ]]
then
    PERCENTAGETOREMOVE=$(echo "$TESTSUITESAMPLE * 0.01" | bc -l )
    echo "sample = $PERCENTAGETOREMOVE" >> $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
fi

# get the class names to be repaired

defects4j export -p classes.modified > $BUGWD/bugfiles.txt

echo "This is the working directory: "
echo $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$WD
