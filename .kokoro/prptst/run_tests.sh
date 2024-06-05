#!/bin/bash
# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
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

# Confirm that the environment has Java version(s) specified
if [[ -z ${JAVA_VERSION+x} ]]; then
    echo -e "'JAVA_VERSION' env var should be a comma delimited list of valid java versions."
    exit 1
fi

# If on kokoro, cd into repo root
if [ -n "$KOKORO_GFILE_DIR" ]; then
  cd github/java-docs-samples || exit
fi

# Print out environment setup
apt update && apt -y upgrade google-cloud-sdk

echo "********** GIT INFO ***********"
git version
echo "********** GCLOUD INFO ***********"
gcloud -v
echo "********** MAVEN INFO  ***********"
mvn -v
echo "********** GRADLE INFO ***********"
gradle -v

# Setup required env variables
export GOOGLE_CLOUD_PROJECT="tpczero-system:java-docs-samples-testing"
export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/secrets/prptst-java-docs-samples-service-account.json
export JAVA_DOCS_COMPUTE_TEST_ZONES="u-us-prp1-a,u-us-prp1-b,u-us-prp1-c"
export JAVA_DOCS_COMPUTE_TEST_IMAGE_PROJECT="tpczero-system:java-docs-samples-testing" # test will fail anyway because images are not there

mkdir -p "${KOKORO_GFILE_DIR}/secrets"
# read secrets from GDU project 'java-docs-samples-testing'
gcloud secrets versions access latest --project="java-docs-samples-testing" --secret="prptst-java-docs-samples-service-account" > "${KOKORO_GFILE_DIR}/secrets/prptst-java-docs-samples-service-account.json"

# Add PRPTST configuration to gcloud CLI (becomes active)
gcloud config configurations create prptst
gcloud config set universe_domain apis-tpczero.goog
gcloud config set api_endpoint_overrides/compute https://compute.apis-tpczero.goog/compute/v1/

# Activate PRPTST service account
gcloud auth activate-service-account --key-file="$GOOGLE_APPLICATION_CREDENTIALS" --project="$GOOGLE_CLOUD_PROJECT"


# Execute compute/cloud-client tests
git config --global --add safe.directory $PWD

project_root="$(git rev-parse --show-toplevel)"

# Fail the tests if no Java version was found.
POM_JAVA=$(grep -oP '(?<=<maven.compiler.target>).*?(?=</maven.compiler.target>)' pom.xml)
ALLOWED_VERSIONS=("1.8" "11" "17" "21")
# shellcheck disable=SC2199
# shellcheck disable=SC2076
if [[ "$POM_JAVA" = "" ]] || [[ ! " ${ALLOWED_VERSIONS[*]} " =~ " ${POM_JAVA} " ]]; then
    RTN=1
    echo -e "\n Testing failed: Unable to determine Java version. Please set in pom:"
    echo -e "\n<properties>"
    echo -e "  <maven.compiler.target>1.8</maven.compiler.target>"
    echo -e "  <maven.compiler.source>1.8</maven.compiler.source>"
    echo -e "</properties>\n"
    exit 1
fi

# Skip tests that don't have the correct Java version.
# shellcheck disable=SC2076
if ! [[ ",$JAVA_VERSION," =~ ",$POM_JAVA," ]]; then
    echo -e "\n Skipping tests: Java version ($POM_JAVA) not required ($JAVA_VERSION)\n"
    exit 0
fi

if [[ (",$JAVA_VERSION," =~ "17" || ",$JAVA_VERSION," =~ "21")  && ( "$file" == *"run/hello-broken"* || "$file" == *"flexible/java-11/pubsub"* || "$file" == *"flexible/java-11/cloudstorage"*|| "$file" == *"flexible/java-11/datastore"*) ]]; then
    echo -e "\n Skipping tests: Sample ($file) tests do not work with Java runtimes 17 or greater\n"
    exit 0
fi


# Use maven to execute the tests for the project.
pushd ${project_root}
make test dir="compute/cloud-client"
EXIT=$?
popd

if [[ $EXIT -ne 0 ]]; then
    RTN=1
    echo -e "\n Testing failed: Maven returned a non-zero exit code. \n"
else
    echo -e "\n Testing completed.\n"
fi

# If this is a periodic build, send the test log to the FlakyBot except for Java 8
# See https://github.com/googleapis/repo-automation-bots/tree/main/packages/flakybot.
if [[ $JAVA_VERSION != "1.8" && $KOKORO_BUILD_ARTIFACTS_SUBDIR = *"periodic"* ]]; then
    chmod +x $KOKORO_GFILE_DIR/linux_amd64/flakybot
    $KOKORO_GFILE_DIR/linux_amd64/flakybot
fi

exit $RTN
