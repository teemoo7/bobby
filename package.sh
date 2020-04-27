#!/bin/sh
set -e
echo "[PACKAGE] Building JAR"
mvn clean install -DskipTests -Dspotbugs.skip=true > /dev/null
echo "[PACKAGE] Building bundled JRE"
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml --output target/jre > /dev/null
echo "[PACKAGE] Creating executable for (Windows)"
launch4j run/launch4j.xml > /dev/null
echo "[PACKAGE] Adding script for UNIX"
cp run/bobby.sh target > /dev/null
chmod 755 target/bobby.sh
echo "[PACKAGE] Archiving all in a ZIP"
cd target > /dev/null
zip -r bobby.zip jre bobby.exe bobby.sh bobby-1.0-SNAPSHOT.jar > /dev/null
cd .. > /dev/null
echo "[PACKAGE] Done! ZIP created in target/bobby.zip"
