#!/bin/bash

echo "Starting Spring Boot application with system initialization..."

# Set the environment variable to enable initialization
export RUN_INIT_STATE=1

echo "DEBUG: Environment variable RUN_INIT_STATE set to: $RUN_INIT_STATE"

# Run the Spring Boot application with the environment variable as a command line argument
echo "DEBUG: About to run: mvn spring-boot:run -Dspring-boot.run.arguments=--RUN_INIT_STATE=1"
mvn spring-boot:run -Dspring-boot.run.arguments=--RUN_INIT_STATE=1

echo "Application stopped."

lsof -i :8080
kill <PID> 