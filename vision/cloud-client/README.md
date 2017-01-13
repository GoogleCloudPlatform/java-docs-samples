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
mvn clean compile assembly:single
```

You can then run a given `ClassName` via:

```
mvn exec:java -Dexec.mainClass=com.example.vision.ClassName \
    -DpropertyName=propertyValue \
    -Dexec.args="arg1 'arg 2' arg3"
```

### Analyze an image

```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
```

```
java -cp target/vision-google-cloud-samples-1.0.0-jar-with-dependencies.jar \
    com.example.vision.Detect \
    logos "./resources/logos.png"
```
