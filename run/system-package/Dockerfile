# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Use the official maven/Java 11 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.6.2-jdk-11-slim as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn compile assembly:single

# Use the Official OpenJDK image for a lean production stage of our multi-stage build.
# https://hub.docker.com/r/adoptopenjdk/openjdk11/
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM adoptopenjdk/openjdk11:alpine

# [START run_system_package_alpine]
RUN apk --no-cache add graphviz ttf-ubuntu-font-family
# [END run_system_package_alpine]

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/system-package-*.jar /system-package.jar

# Run the web service on container startup.
CMD ["java","-jar","/system-package.jar"]
