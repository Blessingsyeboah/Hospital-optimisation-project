#!/usr/bin/env bash
# Builds the ENTIRE monorepo: shared -> pod2 -> pod3 -> pod4 -> pod5 -> pod1 (app).
# No Maven, no internet connection required -- just a JDK (17+) and the
# vendored jar in lib/. Run this from the repo root.
set -e
cd "$(dirname "$0")"

# Classpath separator: ':' on Linux/macOS, ';' on Windows (Git Bash/MSYS/Cygwin).
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *) SEP=":" ;;
esac

echo "== Cleaning build/ =="
rm -rf build
mkdir -p build/app

CP="lib/sqlite-jdbc.jar"

echo "== Compiling shared =="
javac -d build/app -cp "$CP" $(find shared/src/main/java -name "*.java")

echo "== Compiling pod2_lists_search_sort =="
javac -d build/app -cp "$CP$SEP""build/app" $(find pod2_lists_search_sort/src/main/java -name "*.java")

echo "== Compiling pod3_queues_priority =="
javac -d build/app -cp "$CP$SEP""build/app" $(find pod3_queues_priority/src/main/java -name "*.java")

echo "== Compiling pod4_trees_lookup =="
javac -d build/app -cp "$CP$SEP""build/app" $(find pod4_trees_lookup/src/main/java -name "*.java")

echo "== Compiling pod5_hashing_grouping_maps =="
javac -d build/app -cp "$CP$SEP""build/app" $(find pod5_hashing_grouping_maps/src/main/java -name "*.java")

echo "== Compiling pod1_data_database (app entry point) =="
javac -d build/app -cp "$CP$SEP""build/app" $(find pod1_data_database/src/main/java -name "*.java")

echo ""
echo "Build succeeded. Run with ./run.sh, test with ./test.sh"
