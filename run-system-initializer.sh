#!/bin/bash

echo "Compiling and running System Initializer..."

# Compile the project
mvn compile

if [ $? -eq 0 ]; then
    echo "Compilation successful. Running System Initializer..."
    
    # Run the SystemInitialiserRunner (standalone version)
    java -cp target/classes ServiceLayer.SystemInitialiserRunner
    
    if [ $? -eq 0 ]; then
        echo "System initialization completed successfully!"
    else
        echo "System initialization failed!"
        exit 1
    fi
else
    echo "Compilation failed!"
    exit 1
fi 