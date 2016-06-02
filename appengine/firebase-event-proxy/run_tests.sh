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
set -x

# Expected to run this file from root of GitHub project.
(
cd appengine/firebase-event-proxy/

expect -c '
spawn dev_appserver.py gae-firebase-listener-python
set python_listener_id $spawn_id
spawn mvn --file gae-firebase-event-proxy appengine:devserver -Dappengine.port=8888
set event_proxy_id $spawn_id
expect {
  # Listen for Got data from Python listener
  -i $python_listener_id "Got data" {
    exit
  } -i $event_proxy_id "Sent" {
    send_user Event proxy sent data successful
  }
}
sleep 20
exit 1
'
)
