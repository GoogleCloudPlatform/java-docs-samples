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
mvn clean package
```

## Quickstart
Transcribe a local audio file
```
mvn exec:java -DQuickstart
```

## Transcribe a audio file
Transcribe a local audio file
```
mvn exec:java -DRecognize -Dexec.args="syncrecognize ./resources/audio.raw"
```

Asynchronously transcribe a local audio file
```
mvn exec:java -DRecognize -Dexec.args="asyncrecognize ./resources/audio.raw"
```

Transcribe a remote audio file
```
mvn exec:java -DRecognize -Dexec.args="syncrecognize gs://cloud-samples-tests/speech/brooklyn.flac"
```

Asynchronously transcribe a remote audio file
```
mvn exec:java -DRecognize -Dexec.args="asyncrecognize gs://cloud-samples-tests/speech/vr.flac"
```

## Transcribe a audio file and print word offsets
Synchronously transcribe an audio file and print word offsets
```
mvn exec:java -DRecognize -Dexec.args="wordoffsets ./resources/audio.raw"
```

Asynchronously transcribe a remote audio file and print word offsets
```
mvn exec:java -DRecognize -Dexec.args="wordoffsets gs://cloud-samples-tests/speech/vr.flac"
```

## Model Selection
Synchronously transcribe an audio file
```
mvn exec:java -DRecognize -Dexec.args="model-selection ./resources/Google_Gnome.wav"
```

Asynchronously transcribe an audio file hosted on GCS
```
mvn exec:java -DRecognize -Dexec.args="model-selection gs://cloud-samples-tests/speech/Google_Gnome.wav"
```

Perform streaming speech transcription on an audio file
```
mvn exec:java -DRecognize -Dexec.args="streamrecognize ./resources/audio.raw"
```

## Auto Punctuation
Synchronously transcribe and punctuate an audio file
```
mvn exec:java -DRecognize -Dexec.args="auto-punctuation ./resources/audio.raw"
```

Asynchronously transcribe and punctuate an audio file hosted on GCS
```
mvn exec:java -DRecognize -Dexec.args="auto-punctuation gs://cloud-samples-tests/speech/brooklyn.flac"
```

Performing streaming speech transcription and punctuation on an audio file
```
mvn exec:java -DRecognize -Dexec.args="stream-punctuation ./resources/audio.raw"
```

Perform microphone streaming speech recognition
```
mvn exec:java -DRecognize -Dexec.args="micstreamrecognize"
```

## Enhanced Model
Transcribe an audio file using an enhanced model
```
mvn exec:java -DRecognize -Dexec.args="enhanced-model ./resources/commercial_mono.wav"
```

## Recognition Metadata
Transcribe an audio file with recognition metadata
```
mvn exec:java -DRecognize -Dexec.args="metadata ./resources/commercial_mono.wav"
```


## Diarization
Transcribe an audio file using diarization on an audio file
```
mvn exec:java -DRecognize -Dexec.args="diarization ./resources/commercial_mono.wav"
```

Transcribe an audio file using diarization on an audio file hosted on GCS
```
mvn exec:java -DRecognize -Dexec.args="diarization gs://cloud-samples-tests/speech/commercial_mono.wav"
```

## Multi-channel
Transcribe an audio file with multiple channels
```
mvn exec:java -DRecognize -Dexec.args="multi-channel ./resources/commercial_stereo.wav"
```

Transcribe an audio file hosted on GCS with multiple channels
```
mvn exec:java -DRecognize -Dexec.args="multi-channel gs://cloud-samples-tests/speech/commercial_stereo.wav"
```

## Multi language
Transcribe an audio file with multiple languages
```
mvn exec:java -DRecognize -Dexec.args="multi-language ./resources/Google_Gnome.wav"
```

Transcribe an audio file hosted on GCS with multiple languages
```
mvn exec:java -DRecognize -Dexec.args="multi-language gs://cloud-samples-tests/speech/Google_Gnome.wav"
```

## Word level confidence
Transcribe an audio file with word level confidence
```
mvn exec:java -DRecognize -Dexec.args="word-level-conf ./resources/audio.raw"
```

Transcribe an audio file hosted on GCS with word level confidence
```
mvn exec:java -DRecognize -Dexec.args="word-level-conf gs://cloud-samples-tests/speech/brooklyn.flac"
```

