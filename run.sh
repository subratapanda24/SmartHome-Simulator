#!/bin/bash
set -e

BUILD_DIR="out"
MAIN_CLASS="app.Main"

echo "=== Smart Home Appliance Control Simulator ==="
echo "Compiling source files..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" $(find src -name "*.java")

echo "Compilation successful. Launching application..."
java -cp "$BUILD_DIR" "$MAIN_CLASS"
