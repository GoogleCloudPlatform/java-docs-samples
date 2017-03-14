#!/usr/bin/env bash

# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -xe

mvn clean appengine:update \
  -Dappengine.additionalParams="--service_account_json_key_file=${GOOGLE_APPLICATION_CREDENTIALS}" \
  -Dappengine.appId="${GOOGLE_PROJECT_ID}" \
  -Dappengine.version="${GOOGLE_VERSION_ID}" \
  -DskipTests=true

curl -f "http://${GOOGLE_VERSION_ID}-dot-${GOOGLE_PROJECT_ID}.appspot.com/"
