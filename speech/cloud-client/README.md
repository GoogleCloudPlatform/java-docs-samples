# Getting Started with Google Cloud Speech API and the Google Cloud Client libraries

[Google Cloud Speech API][speech] enables easy integration of Google speech
recognition technologies into developer applications.

These sample Java applications demonstrate how to access the Cloud Speech API
using the [Google Cloud Client Library for Java][google-cloud-java].

[speech]: https://cloud.google.com/speech/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

  mvn clean compile assembly:single

### Transcribe a local audio file (using the quickstart sample)

    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.QuickstartSample

### Transcribe a local audio file (using the recognize sample)
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize syncrecognize ./resources/audio.raw
```

### Asynchronously transcribe a local audio file (using the recognize sample)
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize asyncrecognize ./resources/audio.raw
```

### Transcribe a remote audio file (using the recognize sample)
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize syncrecognize gs://cloud-samples-tests/speech/brooklyn.flac
```

### Asynchronously transcribe a remote audio file (using the recognize sample)
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize asyncrecognize gs://cloud-samples-tests/speech/vr.flac
```

### Synchronously transcribe an audio file and print word offsets
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize wordoffsets ./resources/audio.raw
```

### Asynchronously transcribe a remote audio file and print word offsets
```
    java -cp target/speech-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.speech.Recognize wordoffsets gs://cloud-samples-tests/speech/vr.flac
```
