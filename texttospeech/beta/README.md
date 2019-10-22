# Google Cloud Text-To-Speech API Java examples

The [Cloud Text To Speech API][texttospeech] enables you to generate and
customize synthesized speech from text or SSML.

These samples show how to list all supported voices, synthesize raw
text, and synthesize a file.

[texttospeech]: https://cloud.google.com/text-to-speech/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Prerequisites

### Download Maven

To get started, [download][maven-download] and [install][maven-install] it.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [TextToSpeech API][text-to-speech-api].
* [Set up][auth] authentication. For
    example, from the Cloud Console, create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```
* Build the samples
    ```
    mvn clean package
    ```

[cloud-console]: https://console.cloud.google.com
[text-to-speech-api]: https://console.cloud.google.com/apis/api/texttospeech.googleapis.com/overview?project=_
[auth]: https://cloud.google.com/docs/authentication/getting-started

## Quckstart
Synthesize text to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/QuickstartSample.java)
```
mvn exec:java -DQuickstart
```

## List Voices
This sample lists all the supported voices. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/ListAllSupportedVoices.java)
```
mvn exec:java -DListVoices
```

## Synthesize Text
This sample synthesizes text to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/SynthesizeText.java)
```
mvn exec:java -DSynthesizeText -Dexec.args='--text "hello"'
```
This sample synthesizes text with an audio profile to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/SynthesizeText.java)
```
mvn exec:java -DSynthesizeText -Dexec.args='--text "hello" "telephony-class-application"'
```
This sample synthesizes ssml to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/SynthesizeText.java)
```
mvn exec:java -DSynthesizeText -Dexec.args='--ssml "<speak>Hello there.</speak>"'
```

## Synthesize File
This sample synthesizes a text file to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/SynthesizeFile.java)
```
mvn exec:java -DSynthesizeFile -Dexec.args='--text resources/hello.txt'
```

This sample synthesizes a ssml file to an output audio file. [Java Code](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/texttospeech/cloud-client/src/main/java/com/example/texttospeech/SynthesizeFile.java)
```
mvn exec:java -DSynthesizeFile -Dexec.args='--ssml resources/hello.ssml'
```
