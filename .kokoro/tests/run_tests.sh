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

# `-e` enables the script to automatically fail when a command fails
# `-o pipefail` sets the exit code to the rightmost comment to exit with a non-zero
set -eo pipefail
# Enables `**` to include files nested inside sub-folders
shopt -s globstar

# `--script-debug` can be added make local testing of this script easier
if [[ $* == *--script-debug* ]]; then
    SCRIPT_DEBUG="true"
    export JAVA_VERSION="1.8"
else
    SCRIPT_DEBUG="false"
fi

# Verify Java versions have been specified
if [[ -z ${JAVA_VERSION+x} ]]; then
    echo -e "'JAVA_VERSION' env var should be a comma delimited list of valid java versions."
    exit 1
fi

# If on kokoro, add btlr to the path and cd into repo root
if [ -n "$KOKORO_GFILE_DIR" ]; then
  bltr_dir="$KOKORO_GFILE_DIR/v0.0.3/"
  chmod +x "${bltr_dir}"btlr
  export PATH="$PATH:$bltr_dir"
  cd github/java-docs-samples || exit
fi

if [[ "$SCRIPT_DEBUG" != "true" ]]; then
    # Update `gcloud` and log versioning for debugging
    apt update && apt -y upgrade google-cloud-sdk
    
    echo "********** GCLOUD INFO ***********"
    gcloud -v
    echo "********** MAVEN INFO  ***********"
    mvn -v
    echo "********** GRADLE INFO ***********"
    gradle -v

    # Setup required env variables
    export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
    export TRANSCODER_PROJECT_NUMBER="779844219229" # For Transcoder samples
    export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/secrets/java-docs-samples-service-account.json
    # For Tasks samples
    export QUEUE_ID=my-appengine-queue
    export LOCATION_ID=us-east1
    # For Datalabeling samples to hit the testing endpoint
    export DATALABELING_ENDPOINT="test-datalabeling.sandbox.googleapis.com:443"
    # For Cloud Run filesystem sample
    export FILESTORE_IP_ADDRESS=$(gcloud secrets versions access latest --secret fs-app)
    # For Cloud Support API to reference a parent resource
    export PARENT_RESOURCE="projects/206675117367"
    
    SECRET_FILES=("java-docs-samples-service-account.json" \
    "java-aws-samples-secrets.txt" \
    "java-dlp-samples-secrets.txt" \
    "java-bigtable-samples-secrets.txt" \
    "java-automl-samples-secrets.txt" \
    "java-functions-samples-secrets.txt" \
    "java-firestore-samples-secrets.txt" \
    "java-cts-v4-samples-secrets.txt" \
    "java-cloud-sql-samples-secrets.txt")

    # create secret dir
    mkdir -p "${KOKORO_GFILE_DIR}/secrets"
    
    for SECRET in "${SECRET_FILES[@]}"; do
      # grab latest version of secret
      gcloud secrets versions access latest --secret="${SECRET%.*}" > "${KOKORO_GFILE_DIR}/secrets/$SECRET"
      # execute secret file contents
      if [[ "$SECRET" != *json ]]; then
        source "${KOKORO_GFILE_DIR}/secrets/$SECRET"
      fi
    done
  
    # Activate service account
    gcloud auth activate-service-account \
        --key-file="$GOOGLE_APPLICATION_CREDENTIALS" \
        --project="$GOOGLE_CLOUD_PROJECT"
fi

# Package local jetty dependency for Java11 samples
if [[ ",$JAVA_VERSION," =~ "11" ]]; then
  cd appengine-java11/appengine-simple-jetty-main/
  mvn install --quiet
  cd ../../
fi

btlr_args=(
    "run"
    "--max-cmd-duration=40m"
    "**/pom.xml"
)

if [ -n "$GIT_DIFF" ]; then
  btlr_args+=(
    "--git-diff"
    "$GIT_DIFF"
  )
fi

echo -e "\n******************** TESTING PROJECTS ********************"
test_prog="$PWD/.kokoro/tests/run_test_java.sh"

# Use btlr to run all the tests in each folder 
echo "btlr" "${btlr_args[@]}" -- "${test_prog}"
btlr "${btlr_args[@]}" -- "${test_prog}"

exit $RTN
