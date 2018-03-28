# OCR Feature Detection Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/beta/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Vision API][vision] provides OCR detection for PDF/TIFF documents.
This API is part of the larger collection of Cloud Machine Learning APIs.

This sample Java application demonstrates how to access the Cloud Vision API
using the [Google Cloud Client Library for Java][google-cloud-java].

[vision]: https://cloud.google.com/vision/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Prerequisites

### Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Vision API][vision-api].
* Set up your environment with [Application Default Credentials][adc]. For
    example, from the Cloud Console, you might create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```
* Build the sample
    ```
    mvn clean package
    ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Samples
You can then run `Detect` via:

```
mvn exec:java -DDetect -Dexec.args="arg1 'arg 2' arg3"
```

#### OCR
```
mvn exec:java -DDetect -Dexec.args="ocr gs://java-docs-samples-testing/vision/HodgeConj.pdf \
   gs://<BUCKET_ID>/"
```
