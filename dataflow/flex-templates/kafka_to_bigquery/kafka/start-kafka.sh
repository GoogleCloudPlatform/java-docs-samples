#!/bin/sh

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
