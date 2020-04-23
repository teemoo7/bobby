#!/bin/sh
set -e
echo "[RELEASE] Building JAR"
mvn clean install -DskipTests -Dspotbugs.skip=true > /dev/null
echo "[RELEASE] Building bundled JRE"
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml --output target/jre > /dev/null
echo "[RELEASE] Creating executable for (Windows)"
launch4j run/launch4j.xml > /dev/null
echo "[RELEASE] Adding script for UNIX"
cp run/bobby.sh target > /dev/null
echo "[RELEASE] Archiving all in a ZIP"
cd target > /dev/null
zip -r bobby.zip jre bobby.exe bobby.sh bobby-1.0-SNAPSHOT.jar > /dev/null
cd .. > /dev/null
echo "[RELEASE] Done! ZIP created in target/bobby.zip"
