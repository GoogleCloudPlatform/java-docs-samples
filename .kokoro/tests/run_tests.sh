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

set -xe
# We spin up some subprocesses. Don't kill them on hangup
trap '' HUP

echo "**** ENVIRONMENT ****"
env

export MAVEN_OPTS='-Xmx800m -Xms400m'

# Temporary directory to store any output to display on error
export ERROR_OUTPUT_DIR="$(mktemp -d)"
trap 'rm -r "${ERROR_OUTPUT_DIR}"' EXIT

# $1 - project
# $2 - PATH
# $3 - search string
function TestIt() {
  curl -s --show-error "https://${1}-${URL}/${2}" | \
  tee -a "${ERROR_OUTPUT_DIR}/response.txt" | \
  grep "${3}"
  if [ "${?}" -ne 0 ]; then
    echo "${1}/${2} ****** NOT FOUND"
  fi
}

export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
export PATH=/google-cloud-sdk/bin:$PATH
source ${KOKORO_GFILE_DIR}/aws-secrets.sh
source ${KOKORO_GFILE_DIR}/dlp_secrets.txt
echo "******** Environment *********"
env
echo "******** mvn & Java *********"
mvn -version

echo "Update gcloud ********"
gcloud components update --quiet

echo "******** activate-service-account ********"
ls -lr ${KOKORO_GFILE_DIR}

gcloud auth activate-service-account\
    --key-file=$GOOGLE_APPLICATION_CREDENTIALS \
    --project=$GOOGLE_CLOUD_PROJECT

echo "********* gcloud config ********"
gcloud config list

echo "******** build everything ********"
cd github/java-docs-samples
mvn -B --fail-at-end clean verify -Dfile.encoding="UTF-16" \
        -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
        -Dbigtable.instanceID=instance | \
     grep -E -v "(^\[INFO\] Download|^\[INFO\].*skipping)"

