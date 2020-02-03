#!/bin/sh

set -e
set -u

# Required environment variables, defined in the Dockerfile:
#   KAFKA_HOME - Directory where Kafka is installed.
#   KAFKA_TOPIC - Kafka topic to create on initialization.
#   KAFKA_ADDRESS - IP address of the exposed Kafka service.
#   KAFKA_PORT - Port to expose and publish for Kafka.
#   ZOOKEEPER_PORT - Port to expose and publish for Zookeeper.

# Function to help set variables in config files.
function update_config {
  config_file="$1"
  name="$2"
  value="$3"

  # Only set the variable if there is a non-empty value.
  if [ ! -z "$value" ]; then
    if egrep -q "^#?$name=" "$config_file"; then
      # If the variable is in the config file, replace its value.
      sed -r -i "s@^#?($name)=.*@\1=$value@" "$config_file"
    else
      # Otherwise, just add it at the end of the file.
      echo "$name=$value" >> "$config_file"
    fi
  fi
}

# Configure and run a single-node Zookeeper instance.
zookeeper_config="$KAFKA_HOME/config/zookeeper.properties"
update_config "$zookeeper_config" 'clientPort' "$ZOOKEEPER_PORT"

# Start Zookeeper in the background, as a daemon.
zookeeper-server-start.sh -daemon "$zookeeper_config"

# Configure and run a single-node Kafka server.
kafka_config="$KAFKA_HOME/config/server.properties"
update_config "$kafka_config" 'advertised.host.name' "$KAFKA_ADDRESS"
update_config "$kafka_config" 'advertised.port' "$KAFKA_PORT"
update_config "$kafka_config" 'zookeeper.connect' "localhost:$ZOOKEEPER_PORT"

# Create the kafka topic in the background, waits until Kafka is running.
sh create-topic.sh "$KAFKA_ADDRESS" "$KAFKA_PORT" "$KAFKA_TOPIC" &

# Start Kafka in the foreground, as the main process.
kafka-server-start.sh "$kafka_config"
