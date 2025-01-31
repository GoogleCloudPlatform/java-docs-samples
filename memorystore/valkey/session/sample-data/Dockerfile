# Use an OpenJDK base image
FROM openjdk:17-jdk-slim

# Install Maven for building the project
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy Maven project files
COPY pom.xml ./
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Copy the built JAR file to the container
RUN cp target/app-1.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]