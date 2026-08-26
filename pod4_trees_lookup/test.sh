#!/usr/bin/env bash
# Builds and runs ONLY this pod's own unit tests.
set -e
cd "$(dirname "$0")"
./build.sh

# Classpath separator: ':' on Linux/macOS, ';' on Windows (Git Bash/MSYS/Cygwin).
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

CP="../lib/sqlite-jdbc.jar${SEP}../build/app"
javac -d ../build/app -cp "$CP" $(find src/test/java -name "*.java")
for cls in $(find src/test/java -name "*.java" -exec basename {} .java \;); do
  PKG=$(head -1 $(find src/test/java -name "${cls}.java") | sed 's/package //;s/;//')
  java -cp "$CP" "${PKG}.${cls}"
done
