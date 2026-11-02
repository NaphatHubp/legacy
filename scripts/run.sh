#!/usr/bin/env bash
# Compile and run the game (macOS / Linux).   Usage: ./scripts/run.sh
set -e
cd "$(dirname "$0")/.."
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
cp -r src/main/resources/. out/        # sprites and tiles must sit next to the compiled classes
java -cp out legacyknight.Main
