#!/bin/sh
clear
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  java -cp ../bin/ TestApp 1923 DELETE ../../resources/file1.pdf
else
  java -cp bin/ TestApp 1923 DELETE resources/file1.pdf
fi
