#!/bin/sh
mvn clean install -DskipTests -Dspotbugs.skip=true
launch4j run/launch4j.xml
cp run/bobby.sh target
cd target
zip -r bobby.zip jre bobby.exe bobby.sh bobby-1.0-SNAPSHOT.jar
cd ..
echo "Done"
