# Image Feature Detection Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/beta/cloud-client/README.md">
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

You can then run `Detect` via:

```
mvn exec:java -DDetect -Dexec.args="arg1 'arg 2' arg3"
```

#### Localized Objects
```
mvn exec:java -DDetect -Dexec.args="object-localization ./resources/puppies.jpg"
```

#### Hand-written OCR
```
mvn exec:java -DDetect -Dexec.args="handwritten-ocr ./resources/handwritten.jpg"
```
