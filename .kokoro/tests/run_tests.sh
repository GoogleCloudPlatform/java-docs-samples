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

# `--debug` can be added make local testing of this script easier
if [[ $* == *--script-debug* ]]; then
    SCRIPT_DEBUG="true"
    JAVA_VERSION="1.8"
else
    SCRIPT_DEBUG="false"
fi

# `--only-changed` will only run tests on projects container changes from the master branch.
if [[ $* == *--only-diff* ]]; then
    ONLY_DIFF="true"
else
    ONLY_DIFF="false"
fi

# Verify Java versions have been specified
if [[ -z ${JAVA_VERSION+x} ]]; then
    echo -e "'JAVA_VERSION' env var should be a comma delimited list of valid java versions."
    exit 1
fi

if [[ "$SCRIPT_DEBUG" != "true" ]]; then
    # Update `gcloud` and log versioning for debugging.
    gcloud components update --quiet
    echo "********** GCLOUD INFO ***********"
    gcloud -v
    echo "********** MAVEN INFO  ***********"
    mvn -v
    echo "********** GRADLE INFO ***********"
    gradle -v

    # Setup required env variables
    export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
    export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
    source "${KOKORO_GFILE_DIR}/aws-secrets.sh"
    source "${KOKORO_GFILE_DIR}/storage-hmac-credentials.sh"
    source "${KOKORO_GFILE_DIR}/dlp_secrets.txt"
    # Activate service account
    gcloud auth activate-service-account \
        --key-file="$GOOGLE_APPLICATION_CREDENTIALS" \
        --project="$GOOGLE_CLOUD_PROJECT"

    cd github/java-docs-samples
fi

# Package local jetty dependency for Java11 samples
if [[ "$JAVA_VERSION" == "11" ]]; then
  cd appengine-java11/appengine-simple-jetty-main/
  mvn install
  cd ../../
fi

echo -e "\n******************** TESTING PROJECTS ********************"
# Switch to 'fail at end' to allow all tests to complete before exiting.
set +e
# Use RTN to return a non-zero value if the test fails.
RTN=0
ROOT=$(pwd)
# Find all POMs in the repository (may break on whitespace).
for file in **/pom.xml; do
    cd "$ROOT"
    # Navigate to the project folder.
    file=$(dirname "$file")
    cd "$file"

    # If $DIFF_ONLY is true, skip projects without changes.
    if [[ "$ONLY_DIFF" = "true" ]]; then
        git diff --quiet origin/master.. .
        CHANGED=$?
        if [[ "$CHANGED" -eq 0 ]]; then
          # echo -e "\n Skipping $file: no changes in folder.\n"
          continue
        fi
    fi

    echo "------------------------------------------------------------"
    echo "- testing $file"
    echo "------------------------------------------------------------"

    # Fail the tests if no Java version was found.
    POM_JAVA=$(grep -oP '(?<=<maven.compiler.target>).*?(?=</maven.compiler.target>)' pom.xml)
    if [[ "$POM_JAVA" = "" ]]; then
        RTN=1
        echo -e "\n Testing failed: Unable to determine Java version. Please set in pom:"
        echo -e "\n<properties>"
        echo -e "  <maven.compiler.target>1.8</maven.compiler.target>"
        echo -e "  <maven.compiler.source>1.8</maven.compiler.source>"
        echo -e "</properties>\n"
        continue
    fi

    # Skip tests that don't have the correct Java version.
    if ! [[ ",$JAVA_VERSION," =~ ",$POM_JAVA," ]]; then
        echo -e "\n Skipping tests: Java version ($POM_JAVA) not required ($JAVA_VERSION)\n"
        continue
    fi

    # Use maven to execute the tests for the project.
    mvn -q --batch-mode --fail-at-end clean verify \
       -Dfile.encoding="UTF-8" \
       -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
       -Dmaven.test.redirectTestOutputToFile=true \
       -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
       -Dbigtable.instanceID=instance
    EXIT=$?

    if [[ $EXIT -ne 0 ]]; then
      RTN=1
      echo -e "\n Testing failed: Maven returned a non-zero exit code. \n"
    else
      echo -e "\n Testing completed.\n"
    fi

done

exit "$RTN"
