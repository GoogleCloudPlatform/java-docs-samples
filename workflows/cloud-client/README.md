# Cloud Workflows Quickstart

The Workflows quickstart allows you to run a Cloud Workflow from a java program.

## Prerequisites

### Download Maven

To get started, [download][maven-download] and [install][maven-install] it.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Workflows API][workflows-api].
* [Set up][auth] authentication. For
    example, from the Cloud Console, create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```
* Build the samples
    ```
    mvn clean package
    ```

[cloud-console]: https://console.cloud.google.com
[workflows-api]: https://console.cloud.google.com/apis/api/workflows.googleapis.com/overview?project=_
[auth]: https://cloud.google.com/docs/authentication/getting-started

## Snippets
To verify the snippets are running correctly, you can run the tests via:
```
mvn clean verify
```

