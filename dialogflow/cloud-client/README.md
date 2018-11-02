# Dialogflow API Java examples

These samples demonstrate the use of the [Dialogflow API][dialogflow].

These samples show how to detect intents with text, audio, and streaming audio.

These samples show how to manage contexts, entities, entity types, and intents

[dialogflow]: https://dialogflow.com/docs/getting-started/basics
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Java Version

This sample requires you to have
[Java8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).

### Download Maven

To get started, [download][maven-download] and [install][maven-install] it.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Dialogflow API][dialogflow-api].
* [Set up][auth] authentication. For
    example, from the Cloud Console, create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_CLOUD_PROJECT=PROJECT_ID
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```
* Set PROJECT_ID in pom.xml to your Google Cloud Project Id.
* Set SESSION_ID in pom.xml to a session name of your choice. (Defaults to SESSION_ID)
* Set CONTEXT_ID in pom.xml to a context name of your choice. (Defaults to CONTEXT_ID)

[cloud-console]: https://console.cloud.google.com
[dialogflow-api]: https://console.cloud.google.com/apis/library/dialogflow.googleapis.com
[auth]: https://cloud.google.com/docs/authentication/getting-started

## Run the Tests

To verify the API's are enabled, run the unit tests via

```bash
mvn clean verify
```