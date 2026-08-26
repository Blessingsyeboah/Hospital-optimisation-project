#!/usr/bin/env bash
# Builds ONLY this pod (plus shared, which every pod depends on).
# Run from inside this pod's folder: ./build.sh
set -e
cd "$(dirname "$0")"
mkdir -p ../build/app
javac -d ../build/app -cp ../lib/sqlite-jdbc.jar $(find ../shared/src/main/java -name "*.java") $(find src/main/java -name "*.java")
echo "Built pod3_queues_priority. Run ./test.sh to test it."
