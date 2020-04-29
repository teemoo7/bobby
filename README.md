# Bobby

[![Build Status](https://travis-ci.org/teemoo7/bobby.svg?branch=master)](https://travis-ci.org/teemoo7/bobby) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ch.teemoo%3Abobby&metric=alert_status)](https://sonarcloud.io/dashboard?id=ch.teemoo%3Abobby) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.teemoo%3Abobby&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.teemoo%3Abobby)

This basic Chess game is a humble tribute to famous Chess player [Robert James "Bobby" Fischer](https://en.wikipedia.org/wiki/Bobby_Fischer), World Chess Champion.

Note that the author does not agree with Fischer's political and religious opinions, but remains a fan of his genius at Chess.

![Bobby Chess Game](src/main/resources/img/logo.png "Bobby Chess Game")

**Note:** requires Java 11

## How to launch

### From a released JAR (coming soon)

If not already done, you can install OpenJDK 11 for free (see the excellent distributions of [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot))
 
Then launch the downloaded JAR:

```
java -jar bobby-1.0.jar 
```

 
### From source code

First build it with maven:

```
mvn clean install
```

Then launch the created JAR:

```
java -jar target/bobby-1.0-SNAPSHOT.jar 
```

## Features

### Game

* AI with multiple strength level, from random-stupid to 3-depths computation, using a minimax algorithm
* All moves implemented
* Change GUI view according to selected color
* Draw proposals
* Undo last move
* Suggest move
* Usage of 15+ famous openings
* Limit computation time to _n_ seconds
* Save game to and load from text files with basic notation
* Load PGN file
* AI is as arrogant as the real Bobby was :smile:

### Technical

* Uses Java 11
* Ability to use bundled light JRE ([doc here](PACKAGE.md))
* Uses [launch4j](run/launch4j.xml) to release it as a Window executable (.exe)
* Strong code coverage, incl. GUI testing
* Code style with Checkstyle, code quality with SpotBugs and SonarSource
* Pipeline with TravisCI
* Uses a nice modern Look & Feel [FlatLaf](https://github.com/JFormDesigner/FlatLaf) for all platforms
* Uses free font _FreeSerif_ in order to have a nice rendering of chess pieces

## Limitations

### Computation time

The implemented AI works uses a depth-first computation, which means that if the computation time is restricted, it may not evaluate every single possible move: it evaluates as deep as possible a first move, then a second, etc, but has no guarantee to cover every move of the first depth.