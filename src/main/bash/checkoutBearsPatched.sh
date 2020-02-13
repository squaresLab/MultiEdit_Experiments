#!/bin/bash

BRANCH="$1"
BEARSDIR="$2"

cd $BEARSDIR || exit
git reset --hard
git checkout $BRANCH
