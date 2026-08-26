#!/usr/bin/env bash
# Runs the console app. Build first with ./build.sh
set -e
cd "$(dirname "$0")"

# Classpath separator: ':' on Linux/macOS, ';' on Windows (Git Bash/MSYS/Cygwin).
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

CP="lib/sqlite-jdbc.jar${SEP}build/app${SEP}shared/src/main/resources"
java -cp "$CP" com.hospitaloptimizer.pod1.Main
