# Video Feature Detection Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=video/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Video Intelligence API][video] provides feature detection for
videos. This API is part of the larger collection of Cloud Machine Learning
APIs.

This sample Java application demonstrates how to access the Cloud Video API
using the [Google Cloud Client Library for Java][google-cloud-java].

[video]: https://cloud.google.com/video-intelligence/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Build the sample

Install [Maven](http://maven.apache.org/).

Build your project with:

```
mvn clean package -DskipTests
```

### Analyze a video
Please follow the [Set Up Your Project](https://cloud.google.com/video-intelligence/docs/getting-started#set_up_your_project)
steps in the Quickstart doc to create a project and enable the Google Cloud
Video Intelligence API. Following those steps, make sure that you
[Set Up a Service Account](https://cloud.google.com/video-intelligence/docs/common/auth#set_up_a_service_account),
and export the following environment variable:

```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
```

After you have authorized, you can analyze videos.

Detect Labels
```
mvn exec:java -DDetect -Dexec.args="labels gs://demomaker/cat.mp4"

mvn exec:java -DDetect -Dexec.args="labels-file ./resources/cat.mp4"
```

Detect Explicit content annotations
```
mvn exec:java -DDetect -Dexec.args="explicit-content gs://demomaker/gbikes_dinosaur.mp4"
```

Detect Shots
```
mvn exec:java -DDetect -Dexec.args="shots gs://demomaker/gbikes_dinosaur.mp4"
```

Transcribe Speech
```
mvn exec:java -DDetect -Dexec.args="speech-transcription gs://python-docs-samples-tests/video/googlework_short.mp4"
```

From Windows, you may need to supply your classpath differently, for example:
```
mvn exec:java -DDetect -Dexec.args="labels gs://demomaker/cat.mp4"
```
or
```
mvn exec:java -DDetect -Dexec.args="labels-file resources\\cat.mp4"
```
