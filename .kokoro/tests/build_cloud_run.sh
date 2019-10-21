#!/bin/bash

# Copyright 2019 Google LLC.
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
requireEnv() {
  test "${!1}" || (echo "Environment Variable '$1' not found" && exit 1)
}
requireEnv SAMPLE_NAME

# Version is in the format <PR#>-<GIT COMMIT SHA>.
# Ensures PR-based triggers of the same branch don't collide if Kokoro attempts
# to run them concurrently.
export SAMPLE_VERSION="${KOKORO_GIT_COMMIT:-latest}"
# Builds not triggered by a PR will fall back to the commit hash then "latest".
SUFFIX=${KOKORO_GITHUB_PULL_REQUEST_NUMBER:-${SAMPLE_VERSION:0:12}}
export SERVICE_NAME="${SAMPLE_NAME}-${SUFFIX}"
export CONTAINER_IMAGE="gcr.io/${GOOGLE_CLOUD_PROJECT}/run-${SAMPLE_NAME}:${SAMPLE_VERSION}"

# Build the service
set -x
gcloud builds submit --tag="${CONTAINER_IMAGE}"
set +x

echo '---'
deploy_to_run.sh

echo
echo '---'
echo

# Register post-test cleanup.
# Only needed if deploy completed.
function cleanup {
  set -x
  gcloud --quiet container images delete "${CONTAINER_IMAGE}" || true
  gcloud beta run services delete ${SERVICE_NAME} \
    --platform=managed \
    --region="${REGION:-us-central1}" \
    --quiet
}
trap cleanup EXIT

# TODO: Perform authentication inside the test.
export ID_TOKEN=$(gcloud auth print-identity-token)
export BASE_URL=$(run_url.sh)
# Do not use exec to preserve trap behavior.
"$@"
