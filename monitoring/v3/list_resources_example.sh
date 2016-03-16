#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass=ListResources -Dexec.args="$1"
