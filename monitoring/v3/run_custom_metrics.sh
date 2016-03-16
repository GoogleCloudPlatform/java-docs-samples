#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass=CreateCustomMetric -Dexec.args="$1"
