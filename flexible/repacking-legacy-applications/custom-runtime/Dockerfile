#  Copyright 2025 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# [START gae_flexible_custom_runtime]

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
# [END gae_flexible_custom_runtime]