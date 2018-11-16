# Google Cloud Vision API Spring boot OCR example

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/landmark-detection/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample takes in the Image and Store it to Google Cloud Storage, and
identifies the Text in it.

## Download Gradle

This sample uses the [Gradle][gradle] build system. Before getting started, be
sure to [download][gradle-download] and [install][gradle-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[gradle]: https://gradle.org/install/
[gradle-download]: https://gradle.org/releases/
[gradle-install]: https://gradle.org/install/

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

* [Create Bucket] (bucket-creation) where you want to Store the Uploaded Image

* Set the Bucket name in the Code

    ```bash
    gcsbucket="gs://Bucket-Name/";
    # Make sure your ACL's are properly set and Bucket has proper access to upload the Image
    # After setting up the ACL, make change accordingly ".setAcl(new ArrayList<>(Arrays.asList(Acl.of(**User.ofAllUsers()**, **Role.READER**)))).build(),"
    ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow
[gcs]: https://cloud.google.com/storage/docs/overview
[bucket-creation] : https://cloud.google.com/storage/docs/creating-buckets

## Run the sample

To build and run the sample:

```bash
./gradlew build
#start the server using the following command
gradlew.bat bootRun

```
