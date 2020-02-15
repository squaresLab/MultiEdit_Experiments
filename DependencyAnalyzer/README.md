The .jar file `jar/DependencyAnalyzer.jar` is executable.

```
Usage: java -jar DependencyAnalyzer.jar
 -Da,--find-anti-dependencies        Find anti data dependencies
                                     (write-after-read).
 -Dc,--find-control-dependencies     Find control dependencies. Do not use
                                     in conjunction with data dependency
                                     analyses.
 -Df,--find-flow-dependencies        Find flow data dependencies
                                     (read-after-write).
 -Do,--find-output-dependencies      Find output data dependencies
                                     (write-after-write).
 -lib,--path-to-soot-libs <path>     Path to Soot's libraries. If you're
                                     running DependencyAnalyzer.jar, then
                                     you don't need to use this option.
                                     Otherwise, use either
                                     jar/DependencyAnalyzer.jar (which
                                     contains all of Soot's dependencies)
                                     or all of the libs in lib/.
 -lines,--lines-to-analyze <line ...>Line(s) to back-slice from. Default
                                     is to slice all lines (which might be
                                     slow).
 -o,--output <path>                  File to write output to. Default is
                                     to print to stdout.
 -Oe,--output-dependency-existence   Output whether there exists a
                                     dependency between specified lines
 -Om,--output-dependency-map         Output a line-to-lines mapping of
                                     dependencies.
 -t,--target <class>                 Target class to analyze.
 -tcp,--target-classpath <path>      Classpath to the analysis target.
```

Example: `java -jar DependencyAnalyzer.jar -t my.pkg.Foo -tcp target/classes/ -Dc -o outputfile -Om -lines 1 2 3 4 5 9000`

To specify what code to analyze, use `-t my.package.Foo` to specify the Java Class and 
`-tcp my/path/to/classfiles` to specify 
the target's classpath (where do the `.class` files for the target reside?).

To specify which analysis to run, use `-Dc` for control dependency analysis. 
Use `-Df`, `-Da`, or `-Do` for data dependency analysis.
Note that you may only choose either `-Dc` xor any combination of `-Df`, `-Da`, and/or `-Do`. 

To specify which lines in code to analyze, use `-lines 1 2 3 4 5 42 9000`. The default option 
is to analyze every line.

To specify the output file to write to, use `-o path/to/outputfile.out`. The default option
is to print to stdout, although Soot will also print its own messages to stdout, so I don't 
suggest redirecting output from stdout.

To specify what type of output to generate, use `-Om` to generate a one-to-many mapping from 
line to dependent lines. Use `-Oe` to output a boolean answer on whether there exists any 
dependencies between the lines specified with `-lines`. If both options are selected, 
then `-Oe` output will print before `-Om` output.

You don't need to worry about `-lib` unless if you're running the analysis in ways other than 
using the JAR (e.g: if you're writing test cases).

Output formatting (`-Oe`):

The output is either `true` or `false`

Output formatting (`-Om`):

The output is a CSV formatted as follows:
```
line1,dependent1ofLine1,dependent2ofLine2, et cetera
line2,dependent1ofLine2,dependent2ofLine2, et cetera
```
