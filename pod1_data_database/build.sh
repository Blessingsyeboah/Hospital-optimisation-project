#!/usr/bin/env bash
# Pod 1's Main.java wires together every other pod, so building it means
# building the whole app. This just calls the root build.sh.
set -e
cd "$(dirname "$0")/.."
./build.sh
