#!/bin/bash

BEARSDIR="$1"
TEST="$2"

#cp lib/jacocoagent.jar $BEARSDIR/
cd $BEARSDIR || exit
#rm -r target
git clean -xdf
mvn install -V -B -DskipTests=true -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true
mvn -Dtest=$TEST -DfailIfNoTests=false test
cd - || exit
find $BEARSDIR -name jacoco.exec
mv $(find $BEARSDIR -name jacoco.exec) ./ && echo "Moved Jacoco file" && exit 0
exit 1
#mv $BEARSDIR/target/jacoco.exec .
#mv $BEARSDIR//modules/activiti-engine/target/jacoco.exec .