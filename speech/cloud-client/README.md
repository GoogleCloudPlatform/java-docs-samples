# Getting Started with Google Cloud Speech API and the Google Cloud Client libraries

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=speech/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Speech API][speech] enables easy integration of Google speech
recognition technologies into developer applications.

These sample Java applications demonstrate how to access the Cloud Speech API
using the [Google Cloud Client Library for Java][google-cloud-java].

[speech]: https://cloud.google.com/speech/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Setup

Install [Maven](http://maven.apache.org/).

Build your project with:

```
mvn clean compile assembly:single
```

## Quickstart
Transcribe a local audio file
```
java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
com.example.speech.QuickstartSample
```

## Transcribe a audio file
Transcribe a local audio file
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize syncrecognize ./resources/audio.raw
```

Asynchronously transcribe a local audio file
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize asyncrecognize ./resources/audio.raw
```

Transcribe a remote audio file
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize syncrecognize gs://cloud-samples-tests/speech/brooklyn.flac
```

Asynchronously transcribe a remote audio file
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize asyncrecognize gs://cloud-samples-tests/speech/vr.flac
```

## Transcribe a audio file and print word offsets
Synchronously transcribe an audio file and print word offsets
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize wordoffsets ./resources/audio.raw
```

Asynchronously transcribe a remote audio file and print word offsets
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize wordoffsets gs://cloud-samples-tests/speech/vr.flac
```

## Transcribe a video file
Synchronously transcribe a video file
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize video ./resources/Google_Gnome.wav
```

Asynchronously transcribe a video file hosted on GCS
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize video gs://cloud-samples-tests/speech/Google_Gnome.wav
```
