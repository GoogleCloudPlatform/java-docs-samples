# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# [START cloudrun_system_package_dockerfile]
# [START run_system_package_dockerfile]
# Use the Official OpenJDK image for a lean production stage of our multi-stage build.
# https://hub.docker.com/r/adoptopenjdk/openjdk11/
FROM adoptopenjdk/openjdk11:alpine-jre

RUN apk --no-cache add graphviz ttf-ubuntu-font-family
# [END run_system_package_dockerfile]
# [END cloudrun_system_package_dockerfile]
