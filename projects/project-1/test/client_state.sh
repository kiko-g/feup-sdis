#!/bin/sh
clear
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  java -cp ../bin/ TestApp 1924 STATE
else
  java -cp bin/ TestApp 1923 STATE
fi
