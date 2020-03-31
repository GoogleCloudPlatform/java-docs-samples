# Service Directory

[Service Directory](https://cloud.google.com/service-directory/) is a platform
for discovering, publishing, and connecting services. It offers customers a
single place to register and discover their services in a consistent and
reliable way, regardless of their environment. These sample Java applications
demonstrate how to access the Service Directory API using the Google Java API
Client Libraries.

## Prerequisites

### Enable the API

You must enable the Service Directory API for your project in order to use these
samples.

### Set Environment Variables

You must set your project ID in order to run the tests

`$export GOOGLE_CLOUD_PROJECT=<your-project-id-here>`

### Grant Permissions

You must ensure that the
[user account or service account](https://cloud.google.com/iam/docs/service-accounts#differences_between_a_service_account_and_a_user_account)
you used to authorize your gcloud session has the proper permissions to edit KMS
resources for your project. In the Cloud Console under IAM, add the `Service
Directory Admin` role to the project whose service account you're using to test.

More information can be found in the
[Authentication docs](https://cloud.google.com/docs/authentication/production).

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

    mvn clean compile assembly:single

You can run the quickstart with:

    java -cp target/servicedirectory-samples-1.0-jar-with-dependencies.jar \
        com.example.Quickstart [your-project-id] [your-location]

You can run the quickstart test with:

    mvn -Dtest='QuickstartTests' test

You can run all tests with:

    mvn clean verify
