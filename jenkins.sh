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
shopt -s globstar
# We spin up some subprocesses. Don't kill them on hangup
trap '' HUP

app_version=""

# shellcheck disable=SC2120
delete_app_version() {
  yes | gcloud --project="${GOOGLE_PROJECT_ID}" \
    app versions delete "${1}"
}
handle_error() {
  errcode=$? # Remember the error code so we can exit with it after cleanup

  # Clean up
  delete_app_version "$@"

  exit ${errcode}
}

# First, style-check the shell scripts
shellcheck ./**/*.sh

# Find all jenkins.sh's and run them.
find . -mindepth 2 -maxdepth 5 -name jenkins.sh -type f | while read -r path; do
  dir="${path%/jenkins.sh}"
  # Use just the first letter of each subdir in version name
  # shellcheck disable=SC2001
  app_version="jenkins-$(echo "${dir#./}" | sed 's#\([a-z]\)[^/]*/#\1-#g')"

  trap 'handle_error $app_version' ERR
  (
  # If there's an error, clean up

  pushd "${dir}"
  # Need different app versions because flex can't deploy over an existing
  # version
  GOOGLE_VERSION_ID="${app_version}" /bin/bash ./jenkins.sh

  # Clean up the app version in the background
  delete_app_version "${app_version}" &
  )
  # Clear the trap
  trap - ERR
done

wait
