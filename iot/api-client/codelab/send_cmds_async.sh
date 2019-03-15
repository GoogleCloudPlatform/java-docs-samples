#
# Copyright 2019 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#!/bin/bash
# Listing the device ids.

color=$1
echo "Changing background color 3 MQTT devices into $color"

for deviceId in 1 2 3
do
  echo "gcloud iot devices commands send --command-data={color:$color} --device=mqtt-device-$deviceId --region=us-central1 --registry=my-registry" > device-send-command-script-$deviceId.sh
  chmod u+x device-send-command-script-$deviceId.sh
done
#call 3 scripts async
./device-send-command-script-1.sh & ./device-send-command-script-2.sh & ./device-send-command-script-3.sh &
sleep 5s
#Clean up
for deviceId in 1 2 3
do
  rm ./device-send-command-script-$deviceId.sh
done
exit 0
