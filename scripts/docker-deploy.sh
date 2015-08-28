#!/bin/sh -x
mvn clean install -DskipTests -Dweb.container.name=profiles_web_1 -Pdocker-deploy
