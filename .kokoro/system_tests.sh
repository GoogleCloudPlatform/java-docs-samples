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
mvn -B --fail-at-end clean verify -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
        -Dbigtable.instanceID=instance | \
     grep -E -v "(^\[INFO\] Download|^\[INFO\].*skipping)"

echo "******** Deploy to prod *******"
cd appengine-java8

for app in "bigtable" "cloudsql" "datastore" "spanner" \
      "urlfetch"
do
  (cd "${app}"
      sed --in-place='.xx' "s/<\/runtime>/<\/runtime><service>${app}<\/service>/" \
          src/main/webapp/WEB-INF/appengine-web.xml
      mvn -B --fail-at-end -q appengine:deploy -Dapp.deploy.version="1" \
          -Dapp.stage.quickstart=true -Dapp.deploy.force=true -Dapp.deploy.promote=true \
          -Dapp.deploy.project="${GOOGLE_CLOUD_PROJECT}" -DskipTests=true
      mv src/main/webapp/WEB-INF/appengine-web.xml.xx src/main/webapp/WEB-INF/appengine-web.xml)
done

echo "******* Test prod Deployed Apps ********"
export URL="dot-${GOOGLE_CLOUD_PROJECT}.appspot.com"

# TestIt "helloworld" "" "Hello App Engine -- Java 8!"


## Run tests using App Engine local devserver.
# test_localhost() {
#   git clone https://github.com/GoogleCloudPlatform/java-repo-tools.git
#
#   devserver_tests=(
#       appengine/helloworld
#       appengine/datastore/indexes
#       appengine/datastore/indexes-exploding
#       appengine/datastore/indexes-perfect
#   )
#   for testdir in "${devserver_tests[@]}" ; do
#     if [ -z "$common_dir" ] || [[ $testdir = $common_dir* ]]; then
#       ./java-repo-tools/scripts/test-localhost.sh appengine "${testdir}"
#     fi
#   done

  # newplugin_std_tests=(
  #     appengine/helloworld-new-plugins
  # )
  # for testdir in "${newplugin_std_tests[@]}" ; do
  #   ./java-repo-tools/scripts/test-localhost.sh standard_mvn "${testdir}"
  #   ./java-repo-tools/scripts/test-localhost.sh standard_gradle "${testdir}"
  # done
}
test_localhost



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
# Shell Check linter.
cd ..
shellcheck ./**/*.sh
