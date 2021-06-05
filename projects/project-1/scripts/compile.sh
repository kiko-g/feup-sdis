#! /usr/bin/bash
# Basic compilation script
# To be executed in the root of the package hierarchy (proj1)
# Compiled code is placed under build/
# If you are using jar files, and these must be in some particular place under the build tree, you should copy/move those jar files.
#rm -rf ../../resources/peer*


# works from proj1/ and from src/
base=$(basename "$(pwd)")

if [ "$base" = "src" ]; then
  javac -d build *.java       # compiles all java files in src/ folder
else
  javac -d src/build src/*.java   # compiles all java files in proj1/src/ folder
fi
