#!/bin/bash
# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail
shopt -s globstar
# We spin up some subprocesses. Don't kill them on hangup
trap '' HUP

# Update gcloud and check version
gcloud components update --quiet
echo "********** GCLOUD INFO ***********"
gcloud -v
echo "********** MAVEN INFO  ***********"
mvn -v
echo "********** GRADLE INFO ***********"
gradle -v

# Setup required enviormental variables
export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
source ${KOKORO_GFILE_DIR}/aws-secrets.sh
source ${KOKORO_GFILE_DIR}/dlp_secrets.txt
# Activate service account
gcloud auth activate-service-account\
    --key-file=$GOOGLE_APPLICATION_CREDENTIALS \
    --project=$GOOGLE_CLOUD_PROJECT

# Run the tests
cd github/java-docs-samples
mvn --batch-mode --fail-at-end clean verify \
    -Dfile.encoding="UTF-8" \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -Dmaven.test.redirectTestOutputToFile=true \
    -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
    -Dbigtable.instanceID=instance
