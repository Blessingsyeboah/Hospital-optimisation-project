#!/usr/bin/env bash
# Compiles and runs EVERY pod's test suite (40+ unit tests total),
# plus the aggregate RunAllTests runner in tests/. Build the app first
# with ./build.sh (test.sh depends on build/app already existing).
set -e
cd "$(dirname "$0")"

if [ ! -d build/app ]; then
  echo "build/app not found -- running ./build.sh first"
  ./build.sh
fi

# Classpath separator: ':' on Linux/macOS, ';' on Windows (Git Bash/MSYS/Cygwin).
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

CP="lib/sqlite-jdbc.jar${SEP}build/app"

echo "== Compiling test sources for every pod =="
javac -d build/app -cp "$CP" $(find pod2_lists_search_sort/src/test/java -name "*.java")
javac -d build/app -cp "$CP" $(find pod3_queues_priority/src/test/java -name "*.java")
javac -d build/app -cp "$CP" $(find pod4_trees_lookup/src/test/java -name "*.java")
javac -d build/app -cp "$CP" $(find pod5_hashing_grouping_maps/src/test/java -name "*.java")
javac -d build/app -cp "$CP" $(find pod1_data_database/src/test/java -name "*.java")
javac -d build/app -cp "$CP" $(find tests/src/main/java -name "*.java")

echo "== Running full suite =="
# run from shared/src/main/resources so DataLoaderTest can find schema.sql
cd shared/src/main/resources
java -cp "../../../../lib/sqlite-jdbc.jar${SEP}../../../../build/app${SEP}." com.hospitaloptimizer.tests.RunAllTests
STATUS=$?
rm -f test_*.db
exit $STATUS
