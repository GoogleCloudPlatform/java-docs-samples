# Google Cloud Vision API Java Face Detection example

## Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

## Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Vision API][vision-api].
* Set up your environment with [Application Default Credentials][adc]. For
    example, from the Cloud Console, you might create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Run the sample

To build and run the sample, run the following from this directory:

```bash
mvn clean compile assembly:single
java -cp target/vision-face-detection-1.0-SNAPSHOT-jar-with-dependencies.jar com.google.cloud.vision.samples.facedetect.FaceDetectApp data/face.jpg output.jpg
```

For more information about face detection see the [Quickstart][quickstart]
guide.

[quickstart]: https://cloud.google.com/vision/docs/face-tutorial
