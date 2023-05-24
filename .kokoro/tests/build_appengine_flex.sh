#!/bin/bash

# Copyright 2023 Google LLC.
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

file="$(pwd)"
APP_REGION="us-central1"
export SAMPLE_VERSION="${KOKORO_GIT_COMMIT:-latest}"
# Builds not triggered by a PR will fall back to the commit hash then "latest".
SUFFIX=${KOKORO_GITHUB_PULL_REQUEST_NUMBER:-${SAMPLE_VERSION:0:12}}-$(date +%s%N)
export SERVICE_NAME="${SAMPLE_NAME}-${SUFFIX}"

set -x

if [[ "$file" == *"hello"* || "$file" == *"static"* ]]; then
  echo "Deploying App Engine Flex project: ${file}"
  mvn clean package appengine:deploy -Dapp.deploy.version=${SERVICE_NAME}

fi
set +x

echo
echo '---'
echo

# Do not use exec to preserve trap behavior.
"$@"

