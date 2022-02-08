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

if [[ "$file" == *"hello-http"* ]]; then
  gcloud functions delete $FUNCTIONS_HTTP_FN_NAME \
    --region="$FUNCTIONS_REGION" -q || true
elif [[ "$file" == *"hello-pubsub"* ]]; then
  gcloud functions delete $FUNCTIONS_PUBSUB_FN_NAME \
    --region="$FUNCTIONS_REGION" -q || true
elif [[ "$file" == *"hello-gcs"* ]]; then
  gcloud functions delete $FUNCTIONS_GCS_FN_NAME \
    --region="$FUNCTIONS_REGION" -q || true
fi
mvn -q -B clean
