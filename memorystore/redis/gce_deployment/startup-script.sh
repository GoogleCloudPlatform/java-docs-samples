#! /bin/bash

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
# [START memorystore_startup_script_sh]
set -ex

# Talk to the metadata server to get the project id and location of application binary.
PROJECTID=$(curl -s "http://metadata.google.internal/computeMetadata/v1/project/project-id" -H "Metadata-Flavor: Google")
GCS_BUCKET_NAME=$(curl -s "http://metadata.google.internal/computeMetadata/v1/instance/attributes/gcs-bucket" -H "Metadata-Flavor: Google")
WAR=$(curl -s "http://metadata.google.internal/computeMetadata/v1/instance/attributes/app-war" -H "Metadata-Flavor: Google")

gsutil cp gs://"$GCS_BUCKET_NAME"/gce/"$WAR" .

# Install dependencies from apt
apt-get update
apt-get install -qq openjdk-8-jdk-headless

# Make Java8 the default
update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

# Jetty Setup
mkdir -p /opt/jetty/temp
mkdir -p /var/log/jetty

# Get Jetty
curl -L https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.10.v20180503/jetty-distribution-9.4.10.v20180503.tar.gz -o jetty9.tgz
tar xf jetty9.tgz  --strip-components=1 -C /opt/jetty

# Add a Jetty User
useradd --user-group --shell /bin/false --home-dir /opt/jetty/temp jetty

cd /opt/jetty
# Add running as "jetty"
java -jar /opt/jetty/start.jar --add-to-startd=setuid
cd /

# very important - by renaming the war to root.war, it will run as the root servlet.
mv $WAR /opt/jetty/webapps/root.war

# Make sure "jetty" owns everything.
chown --recursive jetty /opt/jetty

# Configure the default paths for the Jetty service
cp /opt/jetty/bin/jetty.sh /etc/init.d/jetty
echo "JETTY_HOME=/opt/jetty" > /etc/default/jetty
{
  echo "JETTY_BASE=/opt/jetty"
  echo "TMPDIR=/opt/jetty/temp"
  echo "JAVA_OPTIONS=-Djetty.http.port=8080"
  echo "JETTY_LOGS=/var/log/jetty"
} >> /etc/default/jetty


# Reload daemon to pick up new service
systemctl daemon-reload

# Install logging monitor. The monitor will automatically pickup logs sent to syslog.
curl -s "https://storage.googleapis.com/signals-agents/logging/google-fluentd-install.sh" | bash
service google-fluentd restart &

service jetty start
service jetty check

echo "Startup Complete"
# [END memorystore_startup_script_sh]
