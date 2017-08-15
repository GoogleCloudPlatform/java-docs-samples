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

# Temporary directory to store any output to display on error
export ERROR_OUTPUT_DIR
ERROR_OUTPUT_DIR="$(mktemp -d)"
trap 'rm -r "${ERROR_OUTPUT_DIR}"' EXIT

delete_app_version() {
  yes | gcloud --project="${GOOGLE_PROJECT_ID}" \
    app versions delete "${1}"
}

handle_error() {
  errcode=$? # Remember the error code so we can exit with it after cleanup

  # Clean up remote app version
  delete_app_version "${1}" &

  # Display any errors
  if [ -n "$(find "${2}" -mindepth 1 -print -quit)" ]; then
    cat "${2:?}"/* 1>&2
  fi

  wait

  exit ${errcode}
}

cleanup() {
  delete_app_version "${GOOGLE_VERSION_ID}" &
  ( [ -d "${ERROR_OUTPUT_DIR}" ] && rm -r "${ERROR_OUTPUT_DIR:?}/"* ) || /bin/true
}

export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
export PATH=/google-cloud-sdk/bin:$PATH

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
mvn clean verify | grep -E -v "(^\[INFO\] Download|^\[INFO\].*skipping)"


# (
# # Stop echoing commands, so we don't leak secret env vars
# #   -Pselenium | \ # LV3 20170616 turn off selenium for now.
# set +x
# mvn --batch-mode clean verify \
#   -Dbookshelf.clientID="${OAUTH2_CLIENT_ID}" \
#   -Dbookshelf.clientSecret="${OAUTH2_CLIENT_SECRET}" \
#   -Dbookshelf.bucket="${GCS_BUCKET envvar is unset}" \
#   | \
#   grep -E -v "(^\[INFO\] Download|^\[INFO\].*skipping)"
# )
#
# Test running samples on localhost.
# git clone https://github.com/GoogleCloudPlatform/java-repo-tools.git
# ./java-repo-tools/scripts/test-localhost.sh jetty helloworld-jsp -- -DskipTests=true
# ./java-repo-tools/scripts/test-localhost.sh jetty helloworld-servlet -- -DskipTests=true
# ./java-repo-tools/scripts/test-localhost.sh jetty helloworld-compat -- -DskipTests=true
# ./java-repo-tools/scripts/test-localhost.sh spring-boot helloworld-springboot -- -DskipTests=true

# Check that all shell scripts in this repo (including this one) pass the
Shell Check linter.
shellcheck ./**/*.sh
