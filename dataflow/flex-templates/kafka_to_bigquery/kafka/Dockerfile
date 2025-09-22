# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8-jre-alpine

WORKDIR /usr/bin

# Set versions to use, override them with the `--build-arg` flag.
# https://docs.docker.com/engine/reference/builder/#arg
ARG KAFKA_VERSION="2.4.0"
ARG SCALA_VERSION="2.12"

# Set variables with default values used by the `start-kafka.sh` script.
# Override them with the `--env` or `-e` flag.
# https://docs.docker.com/engine/reference/builder/#env
ENV KAFKA_TOPIC="${KAFKA_TOPIC:-messages}"
ENV KAFKA_ADDRESS="${KAFKA_ADDRESS:-localhost}"
ENV KAFKA_PORT="${KAFKA_PORT:-9092}"
ENV ZOOKEEPER_PORT="${ZOOKEEPER_PORT:-2181}"

# Download and install Apache Kafka.
RUN apk add --no-cache bash \
  && wget https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz \
    -O /tmp/kafka.tgz \
  && tar xzf /tmp/kafka.tgz -C /opt && rm /tmp/kafka.tgz \
  && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka

# Export the KAFKA_HOME and add it to PATH as well.
ENV KAFKA_HOME="/opt/kafka"
ENV PATH="${KAFKA_HOME}/bin:${PATH}"

# Copy all the scripts into the working directory.
COPY *.sh ./

# Expose Zookeeper and Kafka ports for externa communication.
# https://docs.docker.com/engine/reference/builder/#expose
EXPOSE ${ZOOKEEPER_PORT} ${KAFKA_PORT}

# Set the default command to the start-kafka.sh script.
CMD ["start-kafka.sh"]
