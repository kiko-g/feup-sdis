#!/bin/sh
clear
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  java -cp ../bin/ TestApp 1924 RECLAIM 0
else
  java -cp bin/ TestApp 1923 BACKUP resources/file.pdf 1
fi