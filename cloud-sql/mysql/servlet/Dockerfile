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
FROM maven:3.6.2-jdk-8-slim as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Use the Official Jetty image for a lean production stage of our multi-stage build.
# https://hub.docker.com/_/jetty
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM jetty:9.4-jre8

# Copy the exploded WAR directory from the builder stage to the Jetty web application directory.
COPY --from=builder /app/target/tabs-vs-spaces-mysql-*/* $JETTY_BASE/webapps/ROOT/

# No CMD needed since Jetty automatically scans, loads, and starts the web app.
