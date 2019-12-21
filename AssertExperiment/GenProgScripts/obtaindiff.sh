#!/bin/bash

export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/


mkdir $3/"$1""$2"/
mkdir $3/"$1""$2"/bugorig/
mkdir $3/"$1""$2"/fix/


defects4j checkout -p $1 -v "$2"b -w $3/"$1""$2"/bugorig
defects4j checkout -p $1 -v "$2"f -w $3/"$1""$2"/fix
diff -r $3/"$1""$2"/bugorig/$4 $3/"$1""$2"/fix/$4 > $3/"$1""$2"/diff.diff

