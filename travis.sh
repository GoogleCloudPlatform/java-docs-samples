#!/usr/bin/env bash
# Copyright 2016 Google Inc. All Rights Reserved.
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

set -e

# Setup GCP application default credentials before `set -x` echos everything out
if [[ $GCLOUD_SERVICE_KEY ]]; then
  echo "$GCLOUD_SERVICE_KEY" | \
    base64 --decode --ignore-garbage > "${HOME}/google-cloud-service-key.json"
  export GOOGLE_APPLICATION_CREDENTIALS="${HOME}/google-cloud-service-key.json"
fi

set -x
# Set pipefail so that `egrep` does not eat the exit code.
set -o pipefail
shopt -s globstar


SKIP_TESTS=false
if [ -z "$GOOGLE_APPLICATION_CREDENTIALS" ] ; then
  SKIP_TESTS=true
fi

# Finds the closest parent dir that encompasses all changed files, and has a
# pom.xml
travis_changed_files_parent() {
  # If we're not in a PR, forget it
  [ -z "${TRAVIS_PULL_REQUEST-CI_PULL_REQUEST}" ] && return 0

  (
    set +e

    changed="$(git diff --name-only "${TRAVIS_COMMIT-CIRCLE_SHA1}" "${TRAVIS_BRANCH-CIRCLE_BRANCH}")"
    if [ $? -ne 0 ]; then
      # Fall back to git head
      changed="$(git diff --name-only "$(git rev-parse HEAD)" "${TRAVIS_BRANCH-CIRCLE_BRANCH}")"
      [ $? -ne 0 ] && return 0  # Give up. Just run everything.
    fi

    # Find the common prefix
    prefix="$(echo "$changed" | \
      # N: Do this for a pair of lines
      # s: capture the beginning of a line, that's followed by a new line
      #    starting with that capture group. IOW - two lines that start with the
      #    same zero-or-more characters. Replace it with just the capture group
      #    (ie the common prefix).
      # D: Delete the first line of the pair, leaving the second line for the
      #    next pass.
      sed -e 'N;s/^\(.*\).*\n\1.*$/\1\n\1/;D')"

    while [ ! -z "$prefix" ] && [ ! -r "$prefix/pom.xml" ] && [ "${prefix%/*}" != "$prefix" ]; do
      prefix="${prefix%/*}"
    done

    [ -r "$prefix/pom.xml" ] || return 0

    echo "$prefix"
  )
}

common_travis_dir="$(travis_changed_files_parent)"

[ -z "$common_travis_dir" ] || pushd "$common_travis_dir"

# Give Maven a bit more memory
#export MAVEN_OPTS='-XX:+PrintFlagsFinal -Xmx800m -Xms400m'
export MAVEN_OPTS='-Xmx800m -Xms400m'
"${TRAVIS_BUILD_DIR-$HOME/$CIRCLE_PROJECT_REPONAME}"/mvnw \
  --batch-mode clean verify -e \
  -DskipTests=$SKIP_TESTS | \
  egrep -v "(^\[INFO\] Download|^\[INFO\].*skipping)"

[ -z "$common_travis_dir" ] || popd

# Check that all shell scripts in this repo (including this one) pass the
# Shell Check linter.
shellcheck ./**/*.sh

# Run tests using App Engine local devserver.
test_localhost() {
  git clone https://github.com/GoogleCloudPlatform/java-repo-tools.git

  devserver_tests=(
      appengine/helloworld
      appengine/datastore/indexes
      appengine/datastore/indexes-exploding
      appengine/datastore/indexes-perfect
  )
  for testdir in "${devserver_tests[@]}" ; do
    if [ -z "$common_travis_dir" ] || [[ $testdir = $common_travis_dir* ]]; then
      ./java-repo-tools/scripts/test-localhost.sh appengine "${testdir}"
    fi
  done

  # newplugin_std_tests=(
  #     appengine/helloworld-new-plugins
  # )
  # for testdir in "${newplugin_std_tests[@]}" ; do
  #   ./java-repo-tools/scripts/test-localhost.sh standard_mvn "${testdir}"
  #   ./java-repo-tools/scripts/test-localhost.sh standard_gradle "${testdir}"
  # done
}
test_localhost
