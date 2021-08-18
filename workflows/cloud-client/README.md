# Cloud Workflows Quickstart
<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/googleapis/java-dlp&page=editor&open_in_editor=samples/snippets/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

The [Workflows API](https://cloud.google.com/workflows/docs/) provides programmatic access to execute serverless workflows that link series of serverless tasks together in an order you define.

## Setup
- A Google Cloud project with billing enabled
- [Enable](https://console.cloud.google.com/launcher/details/google/workflows.googleapis.com) the DLP API.
- [Create a service account](https://cloud.google.com/docs/authentication/getting-started)
and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable pointing to the downloaded credentials file.

## Snippets
Run the tests via:
```
mvn clean verify
```

