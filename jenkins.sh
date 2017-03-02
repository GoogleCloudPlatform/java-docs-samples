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

set -x
shopt -s globstar

delete_app_version() {
  yes | gcloud --project="$GOOGLE_PROJECT_ID" \
    app versions delete "$GOOGLE_VERSION_ID"
}
handle_error() {
  errcode=$? # Remember the error code so we can exit with it after cleanup

  # Clean up
  delete_app_version

  exit $errcode
}
trap handle_error ERR

# First, style-check the shell scripts
shellcheck ./**/*.sh

# Find all jenkins.sh's and run them.
find . -mindepth 2 -maxdepth 5 -name jenkins.sh -type f | while read path; do
  (
  pushd "${path%jenkins.sh}"
  /bin/bash ./jenkins.sh
  )
done

delete_app_version
