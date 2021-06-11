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
  bltr_dir="$KOKORO_GFILE_DIR/v0.0.2/"
  chmod +x "${bltr_dir}"btlr
  export PATH="$PATH:$bltr_dir"
  cd github/java-docs-samples || exit
fi

if [[ "$SCRIPT_DEBUG" != "true" ]]; then
    # Update `gcloud` and log versioning for debugging.
    gcloud components install beta --quiet
    gcloud components update --quiet
    echo "********** GCLOUD INFO ***********"
    gcloud -v
    echo "********** MAVEN INFO  ***********"
    mvn -v
    echo "********** GRADLE INFO ***********"
    gradle -v

    # Setup required env variables
    export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
    export TRANSCODER_PROJECT_NUMBER="779844219229" # For Transcoder samples
    export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
    # For Tasks samples
    export QUEUE_ID=my-appengine-queue
    export LOCATION_ID=us-east1
    # For Datalabeling samples to hit the testing endpoint
    export DATALABELING_ENDPOINT="test-datalabeling.sandbox.googleapis.com:443"
    # shellcheck source=src/aws-secrets.sh
    source "${KOKORO_GFILE_DIR}/aws-secrets.sh"
    # shellcheck source=src/dlp_secrets.txt
    source "${KOKORO_GFILE_DIR}/dlp_secrets.txt"
    # shellcheck source=src/bigtable_secrets.txt
    source "${KOKORO_GFILE_DIR}/bigtable_secrets.txt"
    # shellcheck source=src/automl_secrets.txt
    source "${KOKORO_GFILE_DIR}/automl_secrets.txt"
    # shellcheck source=src/functions_secrets.txt
    source "${KOKORO_GFILE_DIR}/functions_secrets.txt"
    # spellcheck source=src/firestore_secrets.txt
    source "${KOKORO_GFILE_DIR}/firestore_secrets.txt"
    # spellcheck source=src/cts_v4_secrets.txt
    source "${KOKORO_GFILE_DIR}/cts_v4_secrets.txt"
    # shellcheck source=src/cloud_sql_secrets.txt
    source "${KOKORO_GFILE_DIR}/cloud_sql_secrets.txt"

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
    "--max-cmd-duration=20m"
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
