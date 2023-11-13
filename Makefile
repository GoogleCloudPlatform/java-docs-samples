# Makefile for running typical developer workflow actions.
# To run actions in a subdirectory of the repo:
#   make lint build dir=translate/snippets

INTERFACE_ACTIONS="build test lint"

.ONESHELL: #ease subdirectory work by using the same subshell for all commands
.-PHONY: *

# Default to current dir if not specified.
dir ?= $(shell pwd)

export GOOGLE_CLOUD_PROJECT = ${GOOGLE_SAMPLES_PROJECT}

build:
	cd ${dir}
	mvn compile

test: check-env build
	cd ${dir}
	mvn --quiet --batch-mode --fail-at-end clean verify \
    -Dfile.encoding="UTF-8" \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -Dmaven.test.redirectTestOutputToFile=true \
    -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
    -Dbigtable.instanceID=instance

lint:
	cd ${dir}
	mvn -P lint checkstyle:check

check-env:
ifndef GOOGLE_SAMPLES_PROJECT
	$(error GOOGLE_SAMPLES_PROJECT environment variable is required to perform this action)
endif

list-actions:
	@ echo ${INTERFACE_ACTIONS}

