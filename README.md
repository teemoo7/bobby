# Bobby

[![Build Status](https://travis-ci.org/teemoo7/bobby.svg?branch=master)](https://travis-ci.org/teemoo7/bobby) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ch.teemoo%3Abobby&metric=alert_status)](https://sonarcloud.io/dashboard?id=ch.teemoo%3Abobby) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ch.teemoo%3Abobby&metric=coverage)](https://sonarcloud.io/dashboard?id=ch.teemoo%3Abobby)

This basic Chess game is a humble tribute to famous Chess player [Robert James "Bobby" Fischer](https://en.wikipedia.org/wiki/Bobby_Fischer), World Chess Champion.

Note that the author does not agree with Fischer's political and religious opinions, but remains a fan of his genius at Chess.

![Bobby Chess Game](src/main/resources/img/logo.png "Bobby Chess Game")

## How to launch it

**Note:** requires Java 11 - either install OpenJDK 11 or use the bundled JRE ([doc here](PACKAGE.md))

First build it

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

## Limitations

### Computation time

The implemented AI works uses a depth-first computation, which means that if the computation time is restricted, it may not evaluate every single possible move: it evaluates as deep as possible a first move, then a second, etc, but has no guarantee to cover every move of the first depth.

### Black pawn rendering as a P on Mac OS X  

On Mac OS X, because of the system font _Apple Color Emoji_, black pawns are not correctly rendered: the unicode char is replaced by a an emoji, which is unsupported in Swing GUI. In such case, the pawn is represented by a "P".

A workaround is to move the above font to the user's fonts folder so that it can be temporarily disabled in the Font Book, but it requires to be a sudoer (and/or disable SIP) and may impact the whole system.

```shell script
sudo mv /System/Library/Fonts/Apple\ Color\ Emoji.ttc ~/Library/Fonts/
```