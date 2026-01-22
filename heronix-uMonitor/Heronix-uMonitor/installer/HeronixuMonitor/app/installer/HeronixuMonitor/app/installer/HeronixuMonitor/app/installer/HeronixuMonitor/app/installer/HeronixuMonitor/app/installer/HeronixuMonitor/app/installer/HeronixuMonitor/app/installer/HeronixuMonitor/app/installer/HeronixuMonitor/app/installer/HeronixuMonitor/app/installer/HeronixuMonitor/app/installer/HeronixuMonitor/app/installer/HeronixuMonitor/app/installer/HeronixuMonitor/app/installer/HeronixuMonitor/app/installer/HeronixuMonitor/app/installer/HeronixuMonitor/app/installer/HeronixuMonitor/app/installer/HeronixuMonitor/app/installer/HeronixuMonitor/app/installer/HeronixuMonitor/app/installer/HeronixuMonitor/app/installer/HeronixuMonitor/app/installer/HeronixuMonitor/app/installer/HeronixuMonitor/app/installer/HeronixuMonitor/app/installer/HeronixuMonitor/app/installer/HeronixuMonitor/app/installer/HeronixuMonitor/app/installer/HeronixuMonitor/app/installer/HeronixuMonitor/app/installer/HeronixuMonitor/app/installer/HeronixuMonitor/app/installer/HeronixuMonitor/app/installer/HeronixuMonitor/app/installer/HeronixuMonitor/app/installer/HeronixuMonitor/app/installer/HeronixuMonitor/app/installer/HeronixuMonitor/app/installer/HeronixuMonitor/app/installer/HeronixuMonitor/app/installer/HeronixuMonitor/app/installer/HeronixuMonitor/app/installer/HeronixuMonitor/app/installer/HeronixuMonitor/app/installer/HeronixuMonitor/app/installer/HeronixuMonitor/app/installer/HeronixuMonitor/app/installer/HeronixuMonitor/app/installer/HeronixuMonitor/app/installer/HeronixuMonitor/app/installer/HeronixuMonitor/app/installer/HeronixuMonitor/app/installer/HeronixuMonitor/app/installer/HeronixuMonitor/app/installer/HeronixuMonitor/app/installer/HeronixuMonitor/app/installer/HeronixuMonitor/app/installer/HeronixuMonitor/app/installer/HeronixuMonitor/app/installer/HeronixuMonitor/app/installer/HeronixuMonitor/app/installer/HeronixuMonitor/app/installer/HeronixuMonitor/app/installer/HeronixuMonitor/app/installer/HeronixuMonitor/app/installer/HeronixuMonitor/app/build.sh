#!/bin/bash

echo "Building Heronix uMonitor..."
echo

# Clean previous build
rm -rf bin
mkdir -p bin

# Compile all Java files
echo "Compiling Java files..."
javac -d bin *.java

if [ $? -eq 0 ]; then
    echo
    echo "Build successful!"
    echo
    echo "To run the application:"
    echo "  java -cp bin HeronixuMonitor"
    echo
    echo "To create JAR file, run: ./build-jar.sh"
else
    echo
    echo "Build failed! Please check for errors."
    exit 1
fi
