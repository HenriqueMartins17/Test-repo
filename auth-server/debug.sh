#!/bin/bash -e

./mvnw clean install

JAR_NAME=mixed-application-1.0.0-SNAPSHOT

#java -jar apps/mixed/target/$JAR_NAME.jar

java -jar apps/mixed/target/$JAR_NAME.jar -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
