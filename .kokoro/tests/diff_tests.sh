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

set -e -o pipefail
shopt -s globstar
# We spin up some subprocesses. Don't kill them on hangup
trap '' HUP

# Update gcloud and check version
gcloud components update --quiet
echo -e "\n ********** GCLOUD INFO *********** \n"
gcloud -v
echo -e "\n ********** MAVEN INFO  *********** \n"
mvn -v
echo -e "\n ********** GRADLE INFO *********** "
gradle -v

# Setup required environmental variables
export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
source ${KOKORO_GFILE_DIR}/aws-secrets.sh
source ${KOKORO_GFILE_DIR}/dlp_secrets.txt
# Activate service account
gcloud auth activate-service-account\
    --key-file=$GOOGLE_APPLICATION_CREDENTIALS \
    --project=$GOOGLE_CLOUD_PROJECT

echo -e "\n******************** TESTING AFFECTED PROJECTS ********************"
# Diff to find out what has changed from master
cd github/java-docs-samples
set +e
RESULTS=0
find * -name pom.xml -print0 | sort -z | while read -d $'\0' file
do
    # Navigate to project
    file=$(dirname "$file")
    pushd "$file" > /dev/null


    # Only tests changed projects
    git diff --quiet master.. .
    CHANGED=$?
    # Only test leafs to prevent testing twice
    PARENT=$(grep "<modules>" pom.xml -c)

    # Check for changes to the current folder
    if [ "$CHANGED" -eq 1 ] && [ "$PARENT" -eq 0 ]; then
        echo "------------------------------------------------------------"
        echo "- testing $file"
        echo "------------------------------------------------------------"

        mvn -q --batch-mode --fail-at-end clean verify \
           -Dfile.encoding="UTF-8" \
           -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
           -Dmaven.test.redirectTestOutputToFile=true \
           -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
           -Dbigtable.instanceID=instance \
           || RESULTS=1
        echo -e "\n Tests complete. \n"
    fi

    popd > /dev/null

done

# Return test results
exit $RESULTS
