#!/bin/bash
#
#  Copyright 2018 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# [START memorystore_deploy_sh]
if [ -z "$GCS_BUCKET_NAME" ]; then
    echo "Must set \$GCS_BUCKET_NAME. For example: GCS_BUCKET_NAME=my-bucket"
    exit 1
fi

if [ -z "$ZONE" ]; then
  ZONE=$(gcloud config get-value compute/zone -q)
  echo $ZONE
fi

if [ -z "$WAR" ]; then
  WAR=visitcounter-1.0-SNAPSHOT.war
fi

#Build the WAR package
cd ..
mvn clean package

#Copy the WAR artifact to the GCS bucket location
gsutil cp -r target/${WAR} gs://"$GCS_BUCKET_NAME"/gce/

cd gce_deployment

# Create an instance
gcloud compute instances create my-instance \
    --image-family=debian-9 \
    --image-project=debian-cloud \
    --machine-type=g1-small \
    --scopes cloud-platform \
    --metadata-from-file startup-script=startup-script.sh \
    --metadata gcs-bucket=$GCS_BUCKET_NAME,app-war=$WAR \
    --zone $ZONE \
    --tags http-server

gcloud compute firewall-rules create allow-http-server-8080 \
    --allow tcp:8080 \
    --source-ranges 0.0.0.0/0 \
    --target-tags http-server \
    --description "Allow port 8080 access to http-server"
# [END memorystore_deploy_sh]
