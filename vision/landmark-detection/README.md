# Google Cloud Vision API Java Landmark Detection example

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/landmark-detection/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample takes in the URI for an object in Google Cloud Storage, and
identifies the landmark pictured in it.

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

* Upload an image to [Google Cloud Storage][gcs] (for example, using
    [gsutil][gsutil] or the [GUI][gcs-browser]), and make sure the image is
    accessible to the default credentials you set up. Note that by default,
    Cloud Storage buckets and their associated objects are readable by service
    accounts within the same project. For example, if you're using gsutil, you
    might do:

    ```bash
    gsutil mb gs://<your-project-bucket>
    gsutil cp landmark.jpg gs://<your-project-bucket>/landmark.jpg
    # This step is unnecessary with default permissions, but for completeness,
    # explicitly give the service account access to the image. This email can
    # be found by running:
    # `grep client_email /path/to/your-project-credentials.json`
    gsutil acl ch -u <service-account@your-project.iam.gserviceaccount.com>:R \
        gs://<your-project-bucket>/landmark.jpg
    ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow
[gcs]: https://cloud.google.com/storage/docs/overview
[gsutil]: https://cloud.google.com/storage/docs/gsutil
[gcs-browser]: https://console.cloud.google.com/storage/browser?project=_

## Run the sample

To build and run the sample, run the following from this directory:

```bash
mvn clean compile assembly:single
java -cp target/vision-landmark-detection-1.0-SNAPSHOT-jar-with-dependencies.jar com.google.cloud.vision.samples.landmarkdetection.DetectLandmark "gs://your-project-bucket/landmark.jpg"
```
