#!/bin/bash

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
function TestEndpoints () {
  # Test getGreeting Endpoint (hello world!)
  curl -X GET \
    "https://${1}.appspot.com/_ah/api/helloworld/v1/hellogreeting/0" | \
    grep "hello version-${1}"

  # Test getGreeting Endpoint (goodbye world!)
  curl -X GET \
    "https://${1}.appspot.com/_ah/api/helloworld/v1/hellogreeting/1" | \
    grep "goodbye world!"

  # Test listGreeting Endpoint (hello world! and goodbye world!)
  curl -X GET \
    "https://${1}.appspot.com/_ah/api/helloworld/v1/hellogreeting" | \
    grep "hello world!\|goodbye world!"

  # Test multiply Endpoint (This is a greeting.)
  curl -X POST \
    -H "Content-Type: application/json" \
    --data "{'message':'This is a greeting from instance ${2}'}." \
    "https://${1}.appspot.com/_ah/api/helloworld/v1/hellogreeting/1" | \
    grep "This is a greeting from instance ${2}."
}

# Jenkins provides values for GOOGLE_PROJECT_ID and GOOGLE_VERSION_ID
# Update Greetings.java
sed -i'.bak' -e "s/hello world!/hello version-${GOOGLE_VERSION_ID}!/g" src/main/java/com/example/helloendpoints/Greetings.java

# Test with Maven
# Attempt to clean and deploy generated archetype
mvn clean appengine:deploy \
    -Dapp.deploy.version="${GOOGLE_VERSION_ID}" \
    -DskipTests=true

# End-2-End tests
TestEndpoints $GOOGLE_PROJECT_ID $GOOGLE_VERSION_ID

# Clean and redploy using Gradle
mvn clean

# Test with Gradle
# Delete Version $GOOGLE_VERSION_ID to test Gradle
gcloud -q app services set-traffic default --splits 1=1
gcloud -q app versions delete ${GOOGLE_VERSION_ID}

# Update build.gradle
sed -i'.bak' -e "s/deploy {/deploy {\n version='${GOOGLE_VERSION_ID}'/g" build.gradle

# Deploy Gradle
gradle appengineDeploy

