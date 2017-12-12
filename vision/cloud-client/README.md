# Image Feature Detection Sample

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

```
mvn exec:java -DDetect -Dexec.args="logos './resources/logos.png'"
```
