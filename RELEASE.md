# Release Bobby

## Create Java runtime (JRE)

First, get the list of java modules dependencies:
```shell
jdeps --list-deps target/bobby-1.0-SNAPSHOT.jar
```       

Then create the JRE with the given dependencies:
```shell
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml --output target/jre
```

Then the JAR can be run with the created JRE:
```shell
target/jre/bin/java -jar target/bobby-1.0-SNAPSHOT.jar
```

## Create Windows executable (.exe) to launch it

1. Download `launch4j`.
2. Use file `run/launch4j.xml` as a config file. It will create an executable `target/bobby.exe`

## ZIP it all (experimental)

Create a ZIP file containing:
```
|- jre
bobby-1.0-SNAPSHOT.jar
bobby.exe
bobby.sh
```
