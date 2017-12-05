# java-docs-samples

![Kokoro Build Status](https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/java-docs-samples.png)
[![Coverage Status](https://codecov.io/gh/GoogleCloudPlatform/java-docs-samples/branch/master/graph/badge.svg)](https://codecov.io/gh/GoogleCloudPlatform/java-docs-samples)

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

While this library is still supported, we suggest trying the newer [Cloud Client Library](https://developers.google.com/api-client-library/java/apis/vision/v1) for Google Cloud Vision, especially for new projects. For more information, please see the notice on the [API Client Library Page](https://developers.google.com/api-client-library/java/apis/vision/v1).

This is a repository that contains java code snippets on [Cloud Platform Documentation](https://cloud.google.com/docs/).

Technology Samples:

* [Bigquery](bigquery)
* [Datastore](datastore)
* [Endpoints](endpoints)
* [Identity-Aware Proxy](iap)
* [Key Management Service](kms)
* [Logging](logging)
* [Monitoring](monitoring)
* [Natural Language](language)
* [PubSub](pubsub)
* [Cloud Spanner](spanner)
* [Speech](speech)
* [Cloud Storage](storage)
* [Translate](translate)
* [Vision](vision)

## Credentials Example

The documentation for [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials).

`BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();`

The client library looks for credentials using the following rules:

1. `GOOGLE_APPLICATION_CREDENTIALS` environment variable, pointing to a service account key JSON file path.
2. Cloud SDK credentials `gcloud auth application-default login`
3. App Engine standard environment credentials.
4. Compute Engine credentials.

You can override this behavior using setCredentials in `BigQueryOptions.newBuilder()` by adding `setCredentials(Credentials credentials)` from [ServiceOptions.builder](http://googlecloudplatform.github.io/google-cloud-java/0.12.0/apidocs/com/google/cloud/ServiceOptions.Builder.html#setCredentials-com.google.auth.Credentials-) and [Credentials](http://google.github.io/google-auth-library-java/releases/0.6.0/apidocs/com/google/auth/Credentials.html?is-external=true).
