#!/bin/bash
# Listing the device ids.

color=$1
echo "Changing background color 3 MQTT devices into $color"

for deviceId in 1 2 3
do
  echo "gcloud iot devices commands send --command-data={color:@color} --device=mqtt-device-$deviceId --region=us-central1 --registry=gateway-registry" > device-send-command-script-$deviceId.sh
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
