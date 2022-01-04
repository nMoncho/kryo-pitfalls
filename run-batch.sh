#!/usr/bin/env bash

ITERATIONS=10

echo "Running Space Benchmark..."

for i in {1..$ITERATIONS}; do sbt 'testOnly *SpaceSerializationBenchmark'; done

echo "Running Speed Benchmark (this will take some time)..."

for i in {1..$ITERATIONS}; do sbt 'testOnly *RegistrationBenchmark'; done
