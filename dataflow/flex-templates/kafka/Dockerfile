FROM openjdk:8-jre-alpine

# Set versions to use, override them with the `--build-arg` flag.
# https://docs.docker.com/engine/reference/builder/#arg
ARG KAFKA_VERSION="2.4.0"
ARG SCALA_VERSION="2.12"

# Set variables with default values used by the `start-kafka.sh` script.
ENV KAFKA_TOPIC="${KAFKA_TOPIC:-messages}"
ENV KAFKA_ADDRESS="${KAFKA_ADDRESS:-localhost}"
ENV KAFKA_PORT="${KAFKA_PORT:-9092}"
ENV ZOOKEEPER_PORT="${ZOOKEEPER_PORT:-2181}"

WORKDIR /usr/bin

# Download and install Apache Kafka.
RUN apk add --no-cache bash \
  && wget http://apache.mirrors.spacedump.net/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz \
    -O /tmp/kafka.tgz \
  && tar xzf /tmp/kafka.tgz -C /opt && rm /tmp/kafka.tgz \
  && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka

# Export the KAFKA_HOME and add it to PATH as well.
ENV KAFKA_HOME="/opt/kafka"
ENV PATH="${KAFKA_HOME}/bin:${PATH}"

# Copy everything into the working directory.
COPY . ./

# Expose Zookeeper and Kafka ports for externa communication.
# https://docs.docker.com/engine/reference/builder/#expose
EXPOSE ${ZOOKEEPER_PORT} ${KAFKA_PORT}

# Set the default command to the start-kafka.sh script.
CMD ["start-kafka.sh"]
