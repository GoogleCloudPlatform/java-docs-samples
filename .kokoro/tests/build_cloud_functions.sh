#!/bin/bash

# Copyright 2022 Google LLC.
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
FUNCTIONS_JAVA_RUNTIME="java11"
FUNCTIONS_REGION="us-central1"

requireEnv() {
  test "${!1}" || (echo "Environment Variable '$1' not found" && exit 1)
}
requireEnv "FUNCTIONS_TOPIC"
requireEnv "FUNCTIONS_BUCKET"

# We must explicitly specify function names for event-based functions

# Version is in the format <PR#>-<GIT COMMIT SHA>.
# Ensures PR-based triggers of the same branch don't collide if Kokoro attempts
# to run them concurrently.
export SAMPLE_VERSION="${KOKORO_GIT_COMMIT:-latest}"
# Builds not triggered by a PR will fall back to the commit hash then "latest".
SUFFIX=${KOKORO_GITHUB_PULL_REQUEST_NUMBER:-${SAMPLE_VERSION:0:12}}

export FUNCTIONS_HTTP_FN_NAME="http-${SUFFIX}"
export FUNCTIONS_PUBSUB_FN_NAME="pubsub-${SUFFIX}"
export FUNCTIONS_GCS_FN_NAME="gcs-${SUFFIX}"

# Set identity token (required for functions without --allow-unauthenticated)
export FUNCTIONS_IDENTITY_TOKEN=$(gcloud auth print-identity-token)

# Identify function language
# (Currently only applicable for Pub/Sub functions)
export LANGUAGE="" # Java = empty string
if [[ "$file" == *"scala"* ]]; then
  export LANGUAGE="Scala"
elif [[ "$file" == *"groovy"* ]]; then
  export LANGUAGE="Groovy"
elif [[ "$file" == *"kotlin"* ]]; then
  export LANGUAGE="Kotlin"
fi

# Deploy functions
set -x

for i in {1..5}; do # Retry
  if [[ "$file" == *"hello-http"* ]]; then
    echo "Deploying function HelloHttp to: ${FUNCTIONS_HTTP_FN_NAME}"
    gcloud functions deploy $FUNCTIONS_HTTP_FN_NAME \
      --region $FUNCTIONS_REGION \
      --runtime $FUNCTIONS_JAVA_RUNTIME \
      --entry-point "functions.HelloHttp" \
      --trigger-http \
      && break
  elif [[ "$file" == *"hello-pubsub"* ]]; then
    echo "Deploying function HelloPubSub to: ${FUNCTIONS_PUBSUB_FN_NAME}"
    gcloud functions deploy $FUNCTIONS_PUBSUB_FN_NAME \
      --region $FUNCTIONS_REGION \
      --runtime $FUNCTIONS_JAVA_RUNTIME \
      --entry-point "functions.${LANGUAGE}HelloPubSub" \
      --trigger-topic $FUNCTIONS_SYSTEM_TEST_TOPIC \
      && break
  elif [[ "$file" == *"hello-gcs"* ]]; then
    echo "Deploying function HelloGcs to: ${FUNCTIONS_GCS_FN_NAME}"
    gcloud functions deploy $FUNCTIONS_GCS_FN_NAME \
      --region $FUNCTIONS_REGION \
      --runtime $FUNCTIONS_JAVA_RUNTIME \
      --entry-point "functions.HelloGcs" \
      --trigger-bucket $FUNCTIONS_BUCKET \
      && break
  fi
done

set +x

echo
echo '---'
echo

# Do not use exec to preserve trap behavior.
"$@"
