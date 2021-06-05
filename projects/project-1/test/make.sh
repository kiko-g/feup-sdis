#!/bin/sh
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  rm -rf ../../resources/peer*
  javac -d ../bin ../src/*.java
else
  javac -d bin src/*.java
fi
