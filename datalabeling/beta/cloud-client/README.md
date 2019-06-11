# DataLabeling API Java examples

These samples demonstrate the use of the [DataLabeling API][https://cloud.google.com/datalabeling/].

These samples show how to perform the following actions:
* create / import a dataset and annotation spec sheet.
* create instructions for the labelers.
* start a labeling task for audio, images, text and video.
* export an annotated dataset.


## Prerequisites

This sample requires you to have java [setup](https://cloud.google.com/java/docs/setup).


## Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [DataLabeling API][datalabeling-api].
* [Set up][auth] authentication. For
    example, from the Cloud Console, create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_CLOUD_PROJECT=PROJECT_ID
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```

[cloud-console]: https://console.cloud.google.com
[datalabeling-api]: https://console.cloud.google.com/apis/library/datalabeling.googleapis.com
[auth]: https://cloud.google.com/docs/authentication/getting-started

## Run the Tests

To verify the API's are enabled, run the unit tests via

```bash
mvn clean verify
```