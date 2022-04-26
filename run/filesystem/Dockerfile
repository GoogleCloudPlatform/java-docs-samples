# Copyright 2021 Google LLC
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

# [START cloudrun_fs_dockerfile]

# Use the official maven/Java 11 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.8.5-jdk-11 as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Use AdoptOpenJDK for base image.
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM eclipse-temurin:18-jdk

# Install filesystem dependencies
RUN apt-get update -y && apt-get install -y \
    tini \
    nfs-kernel-server \
    nfs-common \
    && apt-get clean

# Set fallback mount directory
ENV MNT_DIR /mnt/nfs/filestore

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/filesystem-*.jar /filesystem.jar

# Copy the statup script
COPY run.sh ./run.sh
RUN chmod +x ./run.sh

# Use tini to manage zombie processes and signal forwarding
# https://github.com/krallin/tini
ENTRYPOINT ["/usr/bin/tini", "--"]

# Run the web service on container startup.
CMD ["/run.sh"]
# [END cloudrun_fs_dockerfile]