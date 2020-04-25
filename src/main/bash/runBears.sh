#!/bin/bash

BEARSDIR="$1"
TEST="$2"

cd $BEARSDIR || exit
git clean -xdf
timeout 5m mvn install -V -B -DskipTests=true -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true
timeout 5m mvn -Dtest=$TEST -DfailIfNoTests=false test
echo "mvn -Dtest=$TEST -DfailIfNoTests=false test"
cd - || exit
echo "Location of jacoco file:"
find $BEARSDIR -name jacoco.exec
mv $(find $BEARSDIR -name jacoco.exec) ./ && echo "Moved Jacoco file" && exit 0
exit 1