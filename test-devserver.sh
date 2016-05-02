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

# Usage:
#     test-devserver.sh path/to/project
#
# This script runs the local appengine:devserver Maven plugin and verifies that
# a request to http://localhost:8080/ does not return an error code.
#
# As an example, this is useful for verifying that datastore-indexes.xml is
# correct (only if autoGenerate=false and the / handler does all queries used),
# as an example.

set -e
set -x

if [ -z "$1" ]; then
  echo "Missing directory parameter."
  echo "Usage:"
  echo "  $0 path/to/project"
  exit 1
fi

(
cd "$1"
expect -c '
    spawn mvn --batch-mode clean appengine:devserver -DskipTests
    set timeout 600
    expect localhost:8080
    sleep 10
    spawn curl --silent --output /dev/stderr --write-out "%{http_code}" http://localhost:8080/
    expect {
      "200" {
        exit
      }
    }
    exit 1
    '
)

