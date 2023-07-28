#!/bin/bash
# Copyright 2017 Google Inc.
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

# `-e` enables the script to automatically fail when a command fails
# `-o pipefail` sets the exit code to the rightmost comment to exit with a non-zero
set -eo pipefail
# Enables `**` to include files nested inside sub-folders
shopt -s globstar

file="$(pwd)"
# `--script-debug` can be added make local testing of this script easier
if [[ $* == *--script-debug* ]]; then
    SCRIPT_DEBUG="true"
    export JAVA_VERSION="1.8"
else
    SCRIPT_DEBUG="false"
fi

# Verify Java versions have been specified
if [[ -z ${JAVA_VERSION+x} ]]; then
    echo -e "'JAVA_VERSION' env var should be a comma delimited list of valid java versions."
    exit 1
fi

# If on kokoro, add btlr to the path and cd into repo root
if [ -n "$KOKORO_GFILE_DIR" ]; then
  bltr_dir="$KOKORO_GFILE_DIR/v0.0.3/"
  chmod +x "${bltr_dir}"btlr
  export PATH="$PATH:$bltr_dir"
  cd github/java-docs-samples || exit
fi

if [[ "$SCRIPT_DEBUG" != "true" ]]; then
    # Update `gcloud` and log versioning for debugging
    apt update && apt -y upgrade google-cloud-sdk
    
    echo "********** GIT INFO ***********"
    git version
    echo "********** GCLOUD INFO ***********"
    gcloud -v
    echo "********** MAVEN INFO  ***********"
    mvn -v
    echo "********** GRADLE INFO ***********"
    gradle -v

    # Setup required env variables
    export GOOGLE_CLOUD_PROJECT=java-docs-samples-testing
    export TRANSCODER_PROJECT_NUMBER="779844219229" # For Transcoder samples
    export GOOGLE_APPLICATION_CREDENTIALS=${KOKORO_GFILE_DIR}/secrets/java-docs-samples-service-account.json
    # For Tasks samples
    export QUEUE_ID=my-appengine-queue
    export LOCATION_ID=us-east1
    # For Datalabeling samples to hit the testing endpoint
    export DATALABELING_ENDPOINT="test-datalabeling.sandbox.googleapis.com:443"
    # For Cloud Run filesystem sample
    export FILESTORE_IP_ADDRESS=$(gcloud secrets versions access latest --secret fs-app)
    export MNT_DIR=$PWD/run/filesystem
    
    SECRET_FILES=("java-docs-samples-service-account.json" \
    "java-aiplatform-samples-secrets.txt" \
    "java-automl-samples-secrets.txt" \
    "java-bigtable-samples-secrets.txt" \
    "java-cloud-sql-samples-secrets.txt" \
    "java-cts-v4-samples-secrets.txt" \
    "java-dlp-samples-secrets.txt" \
    "java-functions-samples-secrets.txt" \
    "java-firestore-samples-secrets.txt" \
    "java-cts-v4-samples-secrets.txt" \
    "java-cloud-sql-samples-secrets.txt" \
    "java-iam-samples-secrets.txt" \
    "java-scc-samples-secrets.txt" \
    "java-bigqueryconnection-samples-secrets.txt" \
    "java-bigquerydatatransfer-samples-secrets.txt")

    # create secret dir
    mkdir -p "${KOKORO_GFILE_DIR}/secrets"
    
    for SECRET in "${SECRET_FILES[@]}"; do
      # grab latest version of secret
      gcloud secrets versions access latest --secret="${SECRET%.*}" > "${KOKORO_GFILE_DIR}/secrets/$SECRET"
      # execute secret file contents
      if [[ "$SECRET" != *json ]]; then
        source "${KOKORO_GFILE_DIR}/secrets/$SECRET"
      fi
    done

    export STS_AWS_SECRET=`gcloud secrets versions access latest --project cloud-devrel-kokoro-resources --secret=java-storagetransfer-aws`
    export AWS_ACCESS_KEY_ID=`S="$STS_AWS_SECRET" python3 -c 'import json,sys,os;obj=json.loads(os.getenv("S"));print (obj["AccessKeyId"]);'`
    export AWS_SECRET_ACCESS_KEY=`S="$STS_AWS_SECRET" python3 -c 'import json,sys,os;obj=json.loads(os.getenv("S"));print (obj["SecretAccessKey"]);'`
    export STS_AZURE_SECRET=`gcloud secrets versions access latest --project cloud-devrel-kokoro-resources --secret=java-storagetransfer-azure`
    export AZURE_STORAGE_ACCOUNT=`S="$STS_AZURE_SECRET" python3 -c 'import json,sys,os;obj=json.loads(os.getenv("S"));print (obj["StorageAccount"]);'`
    export AZURE_CONNECTION_STRING=`S="$STS_AZURE_SECRET" python3 -c 'import json,sys,os;obj=json.loads(os.getenv("S"));print (obj["ConnectionString"]);'`
    export AZURE_SAS_TOKEN=`S="$STS_AZURE_SECRET" python3 -c 'import json,sys,os;obj=json.loads(os.getenv("S"));print (obj["SAS"]);'`

    # Activate service account
    gcloud auth activate-service-account \
        --key-file="$GOOGLE_APPLICATION_CREDENTIALS" \
        --project="$GOOGLE_CLOUD_PROJECT"
fi

# Package local jetty dependency for Java11 samples
if [[ ",$JAVA_VERSION," =~ "11" ]]; then
  cd appengine-java11/appengine-simple-jetty-main/
  mvn install --quiet
  cd ../../
fi

# Install Chrome and chrome driver for recaptcha tests
if [[ "$file" == *"recaptcha_enterprise/"* ]]; then

  # Based on this content: https://github.com/puppeteer/puppeteer/blob/main/docs/troubleshooting.md#chrome-headless-doesnt-launch-on-unix
  # https://github.com/alixaxel/chrome-aws-lambda/issues/164
  apt install libnss3
  apt install libnss3-dev libgdk-pixbuf2.0-dev libgtk-3-dev libxss-dev libgconf-2-4

  # Install Chrome.
  curl https://dl-ssl.google.com/linux/linux_signing_key.pub -o /tmp/google.pub \
    && cat /tmp/google.pub | apt-key add -; rm /tmp/google.pub \
    && echo 'deb http://dl.google.com/linux/chrome/deb/ stable main' > /etc/apt/sources.list.d/google.list \
    && mkdir -p /usr/share/desktop-directories \
    && apt-get -y update && apt-get install -y google-chrome-stable

  # Disable the SUID sandbox so that Chrome can launch without being in a privileged container.
  dpkg-divert --add --rename --divert /opt/google/chrome/google-chrome.real /opt/google/chrome/google-chrome \
    && echo "#!/bin/bash\nexec /opt/google/chrome/google-chrome.real --no-sandbox --disable-setuid-sandbox \"\$@\"" > /opt/google/chrome/google-chrome \
    && chmod 755 /opt/google/chrome/google-chrome

  # Install chrome driver.
  mkdir -p /opt/selenium \
    && curl http://chromedriver.storage.googleapis.com/`curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE`/chromedriver_linux64.zip -o /opt/selenium/chromedriver_linux64.zip \
    && cd /opt/selenium; unzip /opt/selenium/chromedriver_linux64.zip; rm -rf chromedriver_linux64.zip; ln -fs /opt/selenium/chromedriver /usr/local/bin/chromedriver;

  export CHROME_DRIVER_PATH="$PWD/chromedriver"
  echo "Installing chrome and driver. Path to installation: $CHROME_DRIVER_PATH"
fi

btlr_args=(
    "run"
    "--max-cmd-duration=40m"
    "**/pom.xml"
)

if [ -n "$GIT_DIFF" ]; then
  btlr_args+=(
    "--git-diff"
    "$GIT_DIFF"
  )
fi

echo -e "\n******************** TESTING PROJECTS ********************"
test_prog="$PWD/.kokoro/tests/run_test_java.sh"

git config --global --add safe.directory $PWD

# Use btlr to run all the tests in each folder 
echo "btlr" "${btlr_args[@]}" -- "${test_prog}"
btlr "${btlr_args[@]}" -- "${test_prog}"

exit $RTN
