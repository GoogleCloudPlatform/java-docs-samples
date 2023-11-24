#!/bin/bash

# Copyright 2023 Google LLC.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

current_dir="$PWD"
# Based on this content: https://github.com/puppeteer/puppeteer/blob/main/docs/troubleshooting.md#chrome-headless-doesnt-launch-on-unix
# https://github.com/alixaxel/chrome-aws-lambda/issues/164
echo "Y" | apt install libnss3
echo "Y" | apt install libnss3-dev libgdk-pixbuf2.0-dev libgtk-3-dev libxss-dev libgconf-2-4

# Install Chrome.
curl https://dl-ssl.google.com/linux/linux_signing_key.pub -o /tmp/google.pub \
  && cat /tmp/google.pub | apt-key add -; rm /tmp/google.pub \
  && echo 'deb http://dl.google.com/linux/chrome/deb/ stable main' > /etc/apt/sources.list.d/google.list \
  && mkdir -p /usr/share/desktop-directories \
  && apt-get -y update && apt-get install -y google-chrome-stable

# Disable the SUID sandbox so that Chrome can launch without being in a privileged container.
dpkg-divert --add --rename --divert /opt/google/chrome/google-chrome.real /opt/google/chrome/google-chrome \
  && echo "#!/bin/bash\nexec /opt/google/chrome/google-chrome.real --no-sandbox --disable-setuid-sandbox \"\$@\"" > /opt/google/chrome/google-chrome \
  && chmod 755 /opt/google/chrome/google-chrome \
  && ln -fs /opt/google/chrome/google-chrome /usr/bin/google-chrome

# Install chrome driver.
echo "Y" | mkdir -p /opt/selenium \
  && curl http://chromedriver.storage.googleapis.com/`curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE`/chromedriver_linux64.zip -o /opt/selenium/chromedriver_linux64.zip \
  && cd /opt/selenium; unzip /opt/selenium/chromedriver_linux64.zip; rm -rf chromedriver_linux64.zip; ln -fs /opt/selenium/chromedriver /usr/local/bin/chromedriver;

echo "Installed chrome and driver."

cd "$current_dir"
# Do not use exec to preserve trap behavior.
"$@"
