#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"

JAVAC="javac"
[ -n "$JAVA_HOME" ] && JAVAC="$JAVA_HOME/bin/javac"

rm -rf bin
mkdir -p bin
find src -name "*.java" > fontes.txt
"$JAVAC" --release 21 -encoding UTF-8 -d bin @fontes.txt
rm -f fontes.txt
echo "Compilado em bin/"
