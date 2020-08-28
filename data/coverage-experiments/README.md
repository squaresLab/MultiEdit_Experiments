# Data for coverage experiments

Relevant data:

* `coverage-data-final` 
-- coverage of tests on patch locations in the patched version of the code.
* `coverage-pattern.txt` 
-- same as above, in a different format.
* `buggy-versions` and `buggy-versions-only-bears` 
-- coverage of tests on buggy version of the code. 
Defects4J and Bears data, resp.
* `buggy-versions-locations-only` and `buggy-versions-locations-only-bears` 
-- coverage of tests on patch locations in the buggy version of the code. 
Defects4J and Bears data, resp.


Data is mostly generated from `multiedit.coverage.DoCoverageExperiments` 
and `multiedit.coverage.DoCoverageExperimentsBuggy` with the following exceptions:

**Bugs for which we were unable to run Jacoco**

* Bears 191 (identical)
* Bears 192 (unable to determine)

**Bugs for which Jacoco did not get any coverage result**

* Math 12 (identical)
* Math 35 (disjoint)
* Closure 80 (disjoint)
* Closure 143 (overlap)


**Bugs where the category (identical/overlap/disjoint) 
differed for line coverage and location coverage
(we deferred to the location coverage category)**

(All of these went from overlap to identical)

(We only calculated this for the patch locations; i.e. data in `coverage-data-final`)

* Bears 41 
* Bears 62 
* Bears 80
* Bears 86
* Bears 123
* Mockito 11
* Mockito 25
* Closure 30
* Closure 49
* Closure 85