#!/bin/bash
# FirstAid Hero - Master Test Runner

echo "========================================"
echo "Starting FirstAid Hero Test Suite..."
echo "========================================"

# Layer 1: Isolated Unit Tests (ViewModels, Aggregates)
echo "[1/3] Running Isolated Unit Tests..."
./gradlew testDebugUnitTest
if [ $? -ne 0 ]; then
    echo "Unit tests failed. Exiting pipeline."
    exit 1
fi
echo " Unit tests passed."

# Layer 2: Integration & UI Tests (Room DB, Espresso)
# Note: An Android emulator or connected device must be running for this layer.
echo "[2/3] Running E2E & Database Integration Tests..."
./gradlew connectedAndroidTest
if [ $? -ne 0 ]; then
    echo " Integration tests failed. Exiting pipeline."
    exit 1
fi
echo " Integration tests passed."

# Layer 3: Security & Crash Hardening Validation
echo "[3/3] Running Lint & Code Quality Validation..."
./gradlew lintDebug
if [ $? -ne 0 ]; then
    echo " Linting found potential crash risks or warnings."
    exit 1
fi

echo "========================================"
echo " All pipelines clear! Zero crashes detected."
echo "========================================"
exit 0
