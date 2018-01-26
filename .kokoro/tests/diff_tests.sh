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

# Setup required enviormental variables
export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/service-acct.json
export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
source ${KOKORO_GFILE_DIR}/aws-secrets.sh
source ${KOKORO_GFILE_DIR}/dlp_secrets.txt
# Activate service account
gcloud auth activate-service-account\
    --key-file=$GOOGLE_APPLICATION_CREDENTIALS \
    --project=$GOOGLE_CLOUD_PROJECT

echo -e "\n******************** CHECKING FOR AFFECTED FOLDERS ********************"
# Diff to find out what has changed from master
cd github/java-docs-samples
find ./*/ -name pom.xml -print0 | sort -z | while read -d $'\0' file
do
    file=$(dirname "$file")
    echo "------------------------------------------------------------"
    echo "- checking $file"
    echo "------------------------------------------------------------"


    pushd "$file" > /dev/null
    set +e
    git diff --quiet master.. .
    RTN=$?
    set -e

    # Check for changes to the current folder
    if [ "$RTN" -eq 1 ]; then
        echo -e "\n Change detected. Running tests. \n "
        mvn -q --batch-mode --fail-at-end clean verify \
           -Dfile.encoding="UTF-8" \
           -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
           -Dmaven.test.redirectTestOutputToFile=true \
           -Dbigtable.projectID="${GOOGLE_CLOUD_PROJECT}" \
           -Dbigtable.instanceID=instance
        echo -e " Tests complete. \n"
    else
        echo -e "\n NO change found. \n"
    fi

    popd > /dev/null

done
