#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
./build.sh

# Classpath separator: ':' on Linux/macOS, ';' on Windows (Git Bash/MSYS/Cygwin).
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

CP="lib/sqlite-jdbc.jar${SEP}build/app"
javac -d build/app -cp "$CP" $(find pod1_data_database/src/test/java -name "*.java")
cd shared/src/main/resources
for cls in DataLoaderTest DispatchDeliveryServiceTest; do
  java -cp "../../../../lib/sqlite-jdbc.jar${SEP}../../../../build/app${SEP}." "com.hospitaloptimizer.pod1.${cls}"
done
rm -f test_*.db
