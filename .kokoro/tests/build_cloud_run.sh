#!/bin/bash

# Copyright 2019 Google LLC
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

JIB=$(grep -o '<artifactId>jib-maven-plugin</artifactId>' pom.xml)
if [ -n "$JIB" ]; then
  set -eo pipefail

  # Register post-test cleanup.
  # Only needed if deploy completed.
  function cleanup {
    mvn -q -B clean
    set -x
    sha=$(gcloud artifacts docker images describe $CONTAINER_IMAGE --format="value(image_summary.digest)")
    gcloud artifacts docker images delete $BASE_IMAGE@$sha --quiet --delete-tags --no-user-output-enabled || true
    gcloud run services delete ${SERVICE_NAME} \
      --platform=managed \
      --region="${REGION:-us-central1}" \
      --quiet --no-user-output-enabled
    set +x
  }
  trap cleanup EXIT

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
  # Remove "/" from the Cloud Run service name
  export SERVICE_NAME="${SERVICE_NAME//\//$'-'}"
  export BASE_IMAGE="us-central1-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/containers/run-${SAMPLE_NAME}"
  export CONTAINER_IMAGE="${BASE_IMAGE}:${SAMPLE_VERSION}"
  export SPECIAL_BASE_IMAGE="us-central1-docker.pkg.dev/${GOOGLE_CLOUD_PROJECT}/containers/imagemagick"
  BASE_IMAGE_SAMPLES=("image-processing" "system-packages")

  # Build the service
  set -x
  mvn -q -B jib:build -Dimage="${CONTAINER_IMAGE}" \
    `if [[ "${BASE_IMAGE_SAMPLES[@]}" =~ "${SAMPLE_NAME}" ]]; then echo "-Djib.from.image=${SPECIAL_BASE_IMAGE}"; fi`

  export MEMORY_NEEDED=("image-processing" "idp-sql");  # Samples that need more memory

  gcloud run deploy "${SERVICE_NAME}" \
    --image="${CONTAINER_IMAGE}" \
    --region="${REGION:-us-central1}" \
    --platform=managed \
    --quiet --no-user-output-enabled  \
    `if [[ "${MEMORY_NEEDED[@]}" =~ "${SAMPLE_NAME}" ]]; then echo "--memory 512M"; fi` \
    `if [ $SAMPLE_NAME = "idp-sql" ]; then echo "--update-secrets CLOUD_SQL_CREDENTIALS_SECRET=idp-sql-secret:latest"; fi`

  set +x

  echo
  echo '---'
  echo

  # Do not use exec to preserve trap behavior.
  "$@"

fi
