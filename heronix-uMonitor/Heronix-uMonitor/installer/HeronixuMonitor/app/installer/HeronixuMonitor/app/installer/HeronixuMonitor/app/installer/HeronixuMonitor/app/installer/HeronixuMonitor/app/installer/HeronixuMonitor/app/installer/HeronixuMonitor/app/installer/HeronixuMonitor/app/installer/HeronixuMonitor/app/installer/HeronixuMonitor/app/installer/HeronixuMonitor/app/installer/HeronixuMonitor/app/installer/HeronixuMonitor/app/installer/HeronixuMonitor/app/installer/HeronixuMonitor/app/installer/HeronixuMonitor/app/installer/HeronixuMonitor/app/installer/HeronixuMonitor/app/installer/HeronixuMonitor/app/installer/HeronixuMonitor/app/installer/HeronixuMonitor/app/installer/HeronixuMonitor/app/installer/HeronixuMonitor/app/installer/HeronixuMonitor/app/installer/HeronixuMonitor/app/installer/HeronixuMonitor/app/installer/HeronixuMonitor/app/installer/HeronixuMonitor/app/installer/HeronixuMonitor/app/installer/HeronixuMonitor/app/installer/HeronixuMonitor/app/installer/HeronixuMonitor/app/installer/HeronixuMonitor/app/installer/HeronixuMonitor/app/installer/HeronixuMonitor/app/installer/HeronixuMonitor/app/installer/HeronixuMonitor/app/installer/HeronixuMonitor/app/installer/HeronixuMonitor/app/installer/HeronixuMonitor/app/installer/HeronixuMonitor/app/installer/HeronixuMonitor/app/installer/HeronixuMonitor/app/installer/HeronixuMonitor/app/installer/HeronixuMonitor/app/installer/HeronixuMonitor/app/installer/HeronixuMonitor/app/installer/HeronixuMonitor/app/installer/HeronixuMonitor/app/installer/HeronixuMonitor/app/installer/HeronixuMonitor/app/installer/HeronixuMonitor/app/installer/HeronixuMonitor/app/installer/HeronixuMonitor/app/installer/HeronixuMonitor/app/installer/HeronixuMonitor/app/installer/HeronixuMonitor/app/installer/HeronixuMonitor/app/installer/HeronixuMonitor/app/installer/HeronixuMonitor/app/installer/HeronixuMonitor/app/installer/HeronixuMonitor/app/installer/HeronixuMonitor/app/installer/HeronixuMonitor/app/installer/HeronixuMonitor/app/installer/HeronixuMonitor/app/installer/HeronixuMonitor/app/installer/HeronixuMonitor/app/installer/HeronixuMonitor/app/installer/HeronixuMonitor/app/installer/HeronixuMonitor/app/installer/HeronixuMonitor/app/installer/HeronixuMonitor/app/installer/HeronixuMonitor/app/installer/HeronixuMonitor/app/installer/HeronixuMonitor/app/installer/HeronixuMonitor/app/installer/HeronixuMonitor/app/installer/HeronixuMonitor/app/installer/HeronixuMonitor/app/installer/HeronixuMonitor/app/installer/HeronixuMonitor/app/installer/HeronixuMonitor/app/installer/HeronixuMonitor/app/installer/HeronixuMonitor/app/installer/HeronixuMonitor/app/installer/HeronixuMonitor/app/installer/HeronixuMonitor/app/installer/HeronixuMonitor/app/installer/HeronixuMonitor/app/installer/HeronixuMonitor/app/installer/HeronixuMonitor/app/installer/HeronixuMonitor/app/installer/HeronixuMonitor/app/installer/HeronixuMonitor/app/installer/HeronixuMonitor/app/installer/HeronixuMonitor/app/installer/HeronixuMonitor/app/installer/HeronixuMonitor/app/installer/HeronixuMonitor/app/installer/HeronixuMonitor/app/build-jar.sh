#!/bin/bash

echo "Building Heronix uMonitor JAR..."
echo

# Clean and compile
rm -rf bin
mkdir -p bin

echo "Compiling Java files..."
javac -d bin *.java

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

# Create manifest
echo "Main-Class: HeronixuMonitor" > bin/MANIFEST.MF
echo

# Create JAR
echo "Creating JAR file..."
cd bin
jar cvfm ../HeronixuMonitor.jar MANIFEST.MF *.class
cd ..

if [ $? -eq 0 ]; then
    echo
    echo "========================================"
    echo "SUCCESS! JAR file created!"
    echo "========================================"
    echo
    echo "File: HeronixuMonitor.jar"
    ls -lh HeronixuMonitor.jar
    echo
    echo "To run the application:"
    echo "  java -jar HeronixuMonitor.jar"
    echo
    echo "Or just double-click HeronixuMonitor.jar"
    echo
else
    echo
    echo "JAR creation failed!"
fi
