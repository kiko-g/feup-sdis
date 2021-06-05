#!/bin/sh
clear
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  java -cp ../bin/ TestApp 1923 RESTORE ../../resources/file.pdf
else
  java -cp bin/ TestApp 1923 RESTORE resources/file1.pdf
fi
