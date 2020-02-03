#!/bin/sh

set -e
set -u

address="$1"
port="$2"
topic="$3"

# Wait until Kafka is up and running by checking the port it exposes.
while [ -z "`netstat -lnt | egrep :$port`" ]; do
  sleep 1
done

# Create the topic.
kafka-topics.sh --create \
  --bootstrap-server "$address:$port" \
  --replication-factor 1 \
  --partitions 1 \
  --topic "$topic"
