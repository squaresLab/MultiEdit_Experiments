# Replication instructions for experiments performed for Program Analysis (17-819)

I run this project in Intellij with Maven, so you might want to do that as well.

I use Anaconda to manage the Python environement.
To create the environment, run `conda env create -f environment.yml`.
Then every time you want to activate the environment, run `conda activate multiedit-empiricalstudy`.

## Experiment 1 (previously conducted)

 1) Get coverage data for projects by running the `multiedit.coverage.DoCoverageExperiments` main class.
 It will run the analysis on all bugs in Bears and Defects4J that are listed in `data/multitest_multiedit.txt`.
    
 2) The data files are in `data/coverage-experiments`. 
 I recommend moving these files so that future executions of `multiedit.coverage.DoCoverageExperiments` and `multiedit.coverage.DoCoverageExperimentsBuggy`
 do not overwrite these files.
 If you would like to use the data I've already collected, the data I use for my experiments is in `data/coverage-experiments/coverage-data-final`.
 
 3) The coverage analysis isn't 100% accurate, so manually move some of the bugs between different categories.
 The decisions I made are listed in `data/coverage-experiments/README.md`.
 
 4) To draw the plots, change the `folder` variable in `src/main/python/coverage.py` to the folder where your data is in, contained in a list.
 Then run `src/main/python/coverage.py` from the root folder of this repository (to ensure that the relative paths work).
 
 ## Experiment 2
 
 1) Get coverage data by running the `multiedit.coverage.DoCoverageExperimentsBuggy` main class (a slightly different class than the one in the previous experiment).
 At the top of `multiedit.coverage.DoCoverageExperimentsBuggy`, there is a flag called `intersectPatch`.
 This flag can be toggled to report all coverage results, or only line numbers in the patch.
 
 2) Similarly to the previous experiment, the resulting data can be found in `data/coverage-experiments`.
 The data I used in these experiments can be found in 4 folders:
 `data/coverage-experiments/buggy-versions` and `data/coverage-experiments/buggy-versions-only-bears` for coverage of the entire buggy programs,
 and `data/coverage-experiments/buggy-versions-locations-only` and `data/coverage-experiments/buggy-versions-locations-only-bears` for coverage results for patch locations.
 
 3) This time I didn't do any post processing (but you could!)
 
 4) In `src/main/python/coverage_buggy_program.py`, 
 change the variables at the top for `buggy_coverage_folders` and `exp_1_folder_name` to the folder where you stored the results of step 2 and the folder where you stored the results of experiment 1, respectively.
 Then run `src/main/python/coverage_buggy_program.py` from the root directory of this repository.
 
 ## Experiment 3
 
1) Make sure you have the coverage results from Experiments 1 and 2 (Steps 1-3 in both experiments).

2) At the top of `src/main/python/statements_in_diffs.py`, change the `coverage_buggy` and `coverage_patched` to the folders where the coverage results are stored.

3) Run `src/main/python/statements_in_diffs.py`. 
This produces two outputs: 
`data/uncovered_chunk_classification.json` has the classifications for each deleted/modified locations in the buggy version,
and standard output has the classification for added/modified lines in the patched version as well as basic statistics on the number of lines/chunks.

4) Run `src/main/python/plot_uncovered_chunks.py`. 
This creates a plot of the classification of deleted/modified locations in the buggy version of the programs.
The standard output also has the classifications for all the deleted/modified lines (similar to the output of the previous step).

5) I did the rest of the coverage analysis in a 
[spreadsheet](https://docs.google.com/spreadsheets/d/1PXAEy49vldKyyyk8YjQsgsCblVRyJ-KStf9UGelPIj8/edit?usp=sharing),
 from the output of the previous two steps.