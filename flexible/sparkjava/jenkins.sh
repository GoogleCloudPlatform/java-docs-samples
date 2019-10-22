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

# Fail on non-zero return and print command to stdout
set -xe

# Jenkins Test Script
function runtests () {
  curl -X GET \
    "https://${2}-dot-${1}.appspot.com/api/users" | \
    tee "$ERROR_OUTPUT_DIR/response.json" | \
    grep "^\\["
}

# Jenkins provides values for GOOGLE_PROJECT_ID and GOOGLE_VERSION_ID

# Test with Maven
mvn clean appengine:deploy \
    -Dapp.deploy.version="${GOOGLE_VERSION_ID}" \
    -Dapp.deploy.promote=false

# End-2-End tests
runtests "${GOOGLE_PROJECT_ID}" "${GOOGLE_VERSION_ID}"
