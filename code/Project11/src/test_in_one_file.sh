#!/bin/bash

# Compile JackCompiler.java
echo "Compiling JackCompiler.java..."
javac JackCompiler.java

# Check if compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting..."
    exit 1
fi

# Run JackCompiler on specific test cases
echo "---------------------------------------"
echo "Processing directory: Average"
echo "---------------------------------------"
java JackCompiler ../Test/Average
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for Average. Exiting..."
    exit 1
fi
 

echo "---------------------------------------"
echo "Processing directory: ComplexArrays"
echo "---------------------------------------"
java JackCompiler ../Test/ComplexArrays
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for ComplexArrays. Exiting..."
    exit 1
fi


echo "---------------------------------------"
echo "Processing directory: ConvertToBin"
echo "---------------------------------------"
java JackCompiler ../Test/ConvertToBin
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for ConvertToBin. Exiting..."
    exit 1
fi
 
echo "---------------------------------------"
echo "Processing directory: Pong"
echo "---------------------------------------"
java JackCompiler ../Test/Pong
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for Pong. Exiting..."
    exit 1
fi
 

echo "---------------------------------------"
echo "Processing directory: Seven"
echo "---------------------------------------"
java JackCompiler ../Test/Seven
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for Seven. Exiting..."
    exit 1
fi
 

echo "---------------------------------------"
echo "Processing directory: Square"
echo "---------------------------------------"
java JackCompiler ../Test/Square
if [ $? -ne 0 ]; then
    echo "JackCompiler execution failed for Square. Exiting..."
    exit 1
fi
 

echo "✅ All file processed."