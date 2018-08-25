#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass=com.example.AlertSample -Dexec.args="$1"
