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
#     test-localhost.sh deployment-type path/to/project -- [maven arguments]
#
# This script runs the local appengine:devserver Maven plugin and verifies that
# a request to http://localhost:8080/ does not return an error code.
#
# As an example, this is useful for verifying that datastore-indexes.xml is
# correct (only if autoGenerate=false and the / handler does all queries used),
# as an example.

print_usage () {
  echo "Usage:" >&2
  echo "  $0 server-type path/to/project [-- maven arguments]" >&2
  echo >&2
  echo "server-type can be any of the following:" >&2
  echo "  appengine" >&2
  echo "  jetty" >&2
}

if [[ -z "$1" ]]; then
  echo "Missing server-type parameter." >&2
  print_usage
  exit 1
fi
case $1 in
  appengine)
    mvn_plugin="appengine:devserver"
    server_started_message="localhost:8080"
    ;;
  jetty)
    mvn_plugin="jetty:run-exploded"
    server_started_message="Started Jetty Server"
    ;;
  *)
    print_usage
    exit 1
    ;;
esac

if [[ -z "$2" ]]; then
  echo "Missing directory parameter." >&2
  print_usage
  exit 1
fi
code_path=$2

mvn_command="mvn --batch-mode clean ${mvn_plugin} -DskipTests"
if [[ "$3" == "--" ]]; then
  shift 3
  for mvn_arg in "${@}"; do
    mvn_command="${mvn_command} ${mvn_arg}"
  done
elif [[ -n "$3" ]]; then
  echo "Got unexpected third argument" >&2
  print_usage
  exit 1
fi

set -e
set -x

(
cd "$code_path"
expect -c "
    spawn ${mvn_command}
    set timeout 600
    expect \"${server_started_message}\"
    "'sleep 10
    spawn curl --silent --output /dev/stderr --write-out "%{http_code}" http://localhost:8080/
    expect {
      "200" {
        exit
      }
    }
    exit 1
    '
)

