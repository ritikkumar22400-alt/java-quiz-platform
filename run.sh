#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

echo "============================================"
echo "  Java-Based Online Quiz Platform"
echo "============================================"
echo

mkdir -p out
echo "Compiling sources..."
find src -name '*.java' > /tmp/jq_sources.txt
javac -encoding UTF-8 -d out @/tmp/jq_sources.txt

echo "Starting application..."
java -cp out quizplatform.Main
