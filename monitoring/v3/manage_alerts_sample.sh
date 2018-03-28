#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass=AlertSample -Dexec.args="$1"
