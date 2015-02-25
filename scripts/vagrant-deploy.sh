#!/bin/sh -x
mvn clean install -DskipTests
mvn clean install -DskipTests -Pvagrant-deploy
