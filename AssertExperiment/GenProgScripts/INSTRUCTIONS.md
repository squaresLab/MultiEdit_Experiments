To run Defects4j Script:

Step 1: Set MultiEdit_Home variable to whereever the MultiEdit_Experiments folder is at. For example, if this folder is right under home directory, then we have 

export MultiEdit_Home=~/MultiEdit_Experiment/

Step 2: Set GP4J_Home variable by

export GP4J_HOME=$MultiEdit_Home/AssertExperiment/genprog4java

Step 3: Set D4J_HOME variable to where the bears benchmark repository is. For example, if the folder is right under home directory, then

export BEARSPATH=~/defects4j/

Step 4: run the following command in GenProgScripts directory:

sh runAll.sh [Project_Name] [num] [storage_location] [source_folder] [class_folder]

where [Project_Name] is the name of the project, [num] is the id of the bug you want to run, and [storage_location] is where you want to store the program of the bug during execution of scripts. [source_folder] is the folder in the bug folder containing the java source files, and [classs_folder] is the folder in the bug folder containing the java class files after compilation. For example, to run Lang-1, we would do

sh runAll.sh Lang 1 [storage_location] src/ target

Step 5: The scripts will prompt you to manually determine which chunk each edit lines belongs to. 0 is the base chunk (which is applied to all combinations of chunks), and -1 means discard the edit line, and 1,2,... each represents a chunk.

Step 6: After confirming correctness of the input chunks, wait until the script runs to completion. Then, check the results in AssertExperiment/results folder. There should be a file called Bears[num].out that contains the fitness experiment result of this bug.

If you need more detailed result (like which tests each chunk combination failed), you may check the [storage_location]/Bears[num]/results folder. The number of methos passed in each failed test class and the assertion score of each failed method is listed at the end of the out files.
