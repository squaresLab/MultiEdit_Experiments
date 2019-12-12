# How to run defects4j bugs in genprog4java

To run a single defects4j bug in genprog4java use the runGenProgForBug.sh script

The purpose of this script is to instantiate a defects4j bug and run it in genprog4java to try to find a patch.

## Preconditions:

* The variable D4J_HOME should be directed to the folder where defects4j is installed.
* The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

## Output

The output is a folder created in the $D4J_HOME/5thParameter/1stParam2ndParam/ with log and tar files where all the variants are stored including the patch, if any was found. 

## Parameters:

* 1st param is the patch in upper case (ex: Lang, Chart, Closure, Math, Time)
* 2nd param is the bug number (ex: 1,2,3,4,...)
* 3th param is the option of running the test suite (ex: allHuman, oneHuman, oneGenerated)
* 4th param is the test suite sample size (ex: 1, 100)
* 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)
* 6th param is the repair approach (e.g., gp, trp, par, all)
* 7th param is the initial seed. It will then increase the seeds by adding 1 until it gets to the number in the next param.
* 8th param is the final seed.
* 9th param is on if the purpose is to test only fault loc and not really trying to find a patch. When it has reached the end of fault localization it will stop.
* 10th param is the folder where the java 7 instalation is located
* 11th param is the folder where the java 8 instalation is located
* 12th param is set to \"true\" if negative tests are to be specified using sampled tests else set this to \"false\""
* 13th param is the path to file containing sampled negative tests"
* 14th param is set to \"true\" if positive tests are to be specified using sampled tests else set this to \"false\""
* 15th param is the path to file containing sampled positive tests"

## Example of usage:

./runGenProgForBug.sh Chart 13 allHuman 100 ExamplesCheckedOut gp 1 5 false /usr/lib/jvm/java-1.7.0-openjdk-amd64 /usr/lib/jvm/java-1.8.0-openjdk-amd64 false \\"\\" false \\"\\"
