# Data for coverage experiments

Scripts that use coverage results draw from the folder `coverage-data-final` and the file `coverage-pattern.txt`.

Data is mostly generated from `multiedit.coverage.DoCoverageExperiments` with the following exceptions:

**Bugs for which we were unable to run Jacoco**

* Bears 191 (identical)
* Bears 192 (unable to determine)

**Bugs for which Jacoco did not get any coverage result**

* Math 12 (identical)
* Math 35 (disjoint)

**Bugs where the category (identical/overlap/disjoint) 
differed for line coverage and location coverage
(we deferred to the location coverage category)**

(All of these went from overlap to identical)

* Bears 41 
* Bears 62 
* Bears 80
* Bears 86
* Bears 123
* Mockito 11
* Mockito 25