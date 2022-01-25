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

FUNCTIONS_JAVA_RUNTIME="java11"

# Register post-test cleanup.
# Only needed if deploy completed.
function cleanup {
  set -x
  gcloud functions delete $FUNCTIONS_HTTP_FN_NAME -q || true
  gcloud functions delete $FUNCTIONS_PUBSUB_FN_NAME -q || true
  gcloud functions delete $FUNCTIONS_GCS_FN_NAME -q || true
  mvn -q -B clean
}
trap cleanup EXIT

requireEnv() {
  test "${!1}" || (echo "Environment Variable '$1' not found" && exit 1)
}
requireEnv $FUNCTIONS_TOPIC
requireEnv $FUNCTIONS_BUCKET

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

# Deploy functions
set -x

gcloud functions deploy $FUNCTIONS_HTTP_FN_NAME \
  --runtime $FUNCTIONS_JAVA_RUNTIME \
  --entry-point "functions.HelloHttp" \
  --trigger-http

gcloud functions deploy $FUNCTIONS_GCS_FN_NAME \
  --runtime $FUNCTIONS_JAVA_RUNTIME \
  --entry-point "functions.HelloPubSub" \
  --trigger-topic $FUNCTIONS_TOPIC

gcloud functions deploy $FUNCTIONS_PUBSUB_FN_NAME \
  --runtime $FUNCTIONS_JAVA_RUNTIME \
  --entry-point "functions.HelloGcs" \
  --trigger-bucket $FUNCTIONS_BUCKET

set +x

echo
echo '---'
echo

# Do not use exec to preserve trap behavior.
"$@"
