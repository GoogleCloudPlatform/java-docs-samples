# java-docs-samples

[![Circle-CI Build Status](https://circleci.com/gh/GoogleCloudPlatform/java-docs-samples.svg?style=shield&circle-token=117b41ead030b212fc7d519519ee9262c4f3480b)](https://circleci.com/gh/GoogleCloudPlatform/java-docs-samples)
[![ghit.me](https://ghit.me/badge.svg?repo=GoogleCloudPlatform/java-docs-samples)](https://ghit.me/repo/GoogleCloudPlatform/java-docs-samples)
[![Coverage Status](https://codecov.io/gh/GoogleCloudPlatform/java-docs-samples/branch/master/graph/badge.svg)](https://codecov.io/gh/GoogleCloudPlatform/java-docs-samples)

This is a repository that contains java code snippets on [Cloud Platform Documentation](https://cloud.google.com/docs/).

The repo is organized as follows:

* [App Engine Standard](appengine)
  * [TaskQueue](taskqueue) <!-- shouldn't this be in appengien ?? -->
  * [Unit Tests](unittests)
* [App Engine Flexible](flexible)
* [Compute Engine](compute)

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
