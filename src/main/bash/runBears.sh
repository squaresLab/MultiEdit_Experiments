#!/bin/bash

BEARSDIR="$1"
TEST="$2"

cp lib/jacocoagent.jar $BEARSDIR/
cd $BEARSDIR || exit
rm -r target
rm -r .mvn
mvn install -V -B -DskipTests=true -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -DskipITs=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true
mkdir .mvn
echo '-javaagent:jacocoagent.jar=excludes=org.junit.*,append=false' > .mvn/jvm.config
mvn -Dtest=$TEST test
cd - || exit
mv $BEARSDIR/jacoco.exec .