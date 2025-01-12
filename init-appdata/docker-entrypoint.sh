#!/bin/sh

#loop exec command
for i in $@; do
  java -jar /app/init-appdata.jar $i
done