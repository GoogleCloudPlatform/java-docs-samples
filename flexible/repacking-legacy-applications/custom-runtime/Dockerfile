# Use Maven to build the project with JDK 8
FROM maven:3.8.6-openjdk-8 AS build

# Set working directory
WORKDIR /app

# Copy the application source code
COPY . .

# Build the application
RUN mvn clean package

# Use Jetty as the runtime
FROM jetty:9.4-jdk8

# Set Jetty working directory
WORKDIR /var/lib/jetty/webapps

# Copy the built WAR file
COPY --from=build /app/target/*.war ./ROOT.war

# Expose the default Jetty port
EXPOSE 8080

# Start Jetty correctly
CMD ["java", "-Djetty.base=/var/lib/jetty", "-jar", "/usr/local/jetty/start.jar"]