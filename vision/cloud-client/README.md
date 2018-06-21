# Image Feature Detection Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Vision API][vision] provides feature detection for images.
This API is part of the larger collection of Cloud Machine Learning APIs.

This sample Java application demonstrates how to access the Cloud Vision API
using the [Google Cloud Client Library for Java][google-cloud-java].

[vision]: https://cloud.google.com/vision/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Build the sample

Install [Maven](http://maven.apache.org/).

Build your project with:

```
mvn clean package
```

You can then run a given `ClassName` via:

```
mvn exec:java -DClassName -Dexec.args="arg1 'arg 2' arg3"
```

### Analyze an image

```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
```

#### Quickstart
```
mvn exec:java -DQuickstartSample
```

#### Faces
```
mvn exec:java -DDetect -Dexec.args="faces ./resources/face_no_surprise.jpg"
```

#### Labels
```
mvn exec:java -DDetect -Dexec.args="labels ./resources/wakeupcat.jpg"
```

#### Landmarks
```
mvn exec:java -DDetect -Dexec.args="landmarks ./resources/landmark.jpg"
```

#### Logos
```
mvn exec:java -DDetect -Dexec.args="logos ./resources/logos.png"
```

#### Text
```
mvn exec:java -DDetect -Dexec.args="text ./resources/text.jpg"
```

#### Safe Search
```
mvn exec:java -DDetect -Dexec.args="safe-search ./resources/wakeupcat.jpg"
```

#### Properties
```
mvn exec:java -DDetect -Dexec.args="properties ./resources/city.jpg"
```

#### Web
```
mvn exec:java -DDetect -Dexec.args="web ./resources/landmark.jpg"
```

#### Web Entities
```
mvn exec:java -DDetect -Dexec.args="web-entities ./resources/landmark.jpg"
```

#### Web Entities Include Geo
```
mvn exec:java -DDetect -Dexec.args="web-entities-include-geo ./resources/landmark.jpg"
```

#### Crop
```
mvn exec:java -DDetect -Dexec.args="crop ./resources/landmark.jpg"
```

#### OCR
```
mvn exec:java -DDetect -Dexec.args="ocr gs://java-docs-samples-testing/vision/HodgeConj.pdf \
   gs://<BUCKET_ID>/"
```
