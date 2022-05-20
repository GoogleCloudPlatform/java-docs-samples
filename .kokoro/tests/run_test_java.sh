#!/bin/bash
# Copyright 2021 Google Inc.
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

file="$(pwd)"
SCRIPT_DIR="$(dirname $0)/"

# Fail the tests if no Java version was found.
POM_JAVA=$(grep -oP '(?<=<maven.compiler.target>).*?(?=</maven.compiler.target>)' pom.xml)
ALLOWED_VERSIONS=("1.8" "11" "17")
# shellcheck disable=SC2199
# shellcheck disable=SC2076
if [[ "$POM_JAVA" = "" ]] || [[ !  "${ALLOWED_VERSIONS[@]}" =~ "${POM_JAVA}" ]]; then
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

if [[ ",$JAVA_VERSION," =~ "17" && ( "$file" == *"run/hello-broken"* || "$file" == *"run/filesystem"* ) ]]; then
    echo -e "\n Skipping tests: Sample ($file) tests do not work with Java 17\n"
    exit 0
fi

# Build and deploy Cloud Functions hello-world samples
# (Some of these samples have E2E tests that use deployed functions.)
if [[ "$file" == *"functions/helloworld/"* ]]; then
    source "$SCRIPT_DIR"/build_cloud_functions.sh
    EXIT=$?

    if [[ $EXIT -ne 0 ]]; then
        RTN=1
        echo -e "\n Cloud Functions build/deploy failed: gcloud returned a non-zero exit code. \n"
    else
        echo -e "\n Cloud Functions build/deploy completed.\n"

        # Wait for functions to warm up (and start detecting events)
        sleep 1m
    fi
fi

# Use maven to execute the tests for the project.
mvn --quiet --batch-mode --fail-at-end clean verify \
    -Dfile.encoding="UTF-8" \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    -Dmaven.test.redirectTestOutputToFile=true \
    -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
    -Dbigtable.instanceID=instance
EXIT=$?

# Tear down (deployed) Cloud Functions after deployment tests are run
if [[ "$file" == *"functions/helloworld/"* ]]; then
    source "$SCRIPT_DIR"/teardown_cloud_functions.sh
fi

if [[ $EXIT -ne 0 ]]; then
    RTN=1
    echo -e "\n Testing failed: Maven returned a non-zero exit code. \n"
else
    echo -e "\n Testing completed.\n"
fi

# Build and deploy Cloud Run samples
if [[ "$file" == "run/"* ]]; then
    export SAMPLE_NAME=${file#"run/"}
    # chmod 755 "$SCRIPT_DIR"/build_cloud_run.sh
    "$SCRIPT_DIR"/build_cloud_run.sh
    EXIT=$?

    if [[ $EXIT -ne 0 ]]; then
    RTN=1
    echo -e "\n Cloud Run build/deploy failed: gcloud returned a non-zero exit code. \n"
    else
    echo -e "\n Cloud Run build/deploy completed.\n"
    fi
fi

# If this is a periodic build, send the test log to the FlakyBot.
# See https://github.com/googleapis/repo-automation-bots/tree/main/packages/flakybot.
if [[ $KOKORO_BUILD_ARTIFACTS_SUBDIR = *"periodic"* ]]; then
    chmod +x $KOKORO_GFILE_DIR/linux_amd64/flakybot
    $KOKORO_GFILE_DIR/linux_amd64/flakybot
fi

exit $RTN
