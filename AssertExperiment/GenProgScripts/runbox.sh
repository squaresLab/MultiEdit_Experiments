#!/bin/bash

WORKDIR=$D4J_HOME/$1
PROJ=$2
NUM=$3


export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/


mkdir $WORKDIR

mkdir $WORKDIR/bad

mkdir $WORKDIR/good

defects4j checkout  -p $PROJ -v "$NUM"b -w $WORKDIR/bad

defects4j checkout  -p $PROJ -v "$NUM"f -w $WORKDIR/good



