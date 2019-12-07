# Cloud Key Management Service

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=kms/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Cloud Key Management Service](https://cloud.google.com/kms/) is a
cloud-hosted key management service that lets you manage encryption for your
cloud services the same way you do on-premise. You can generate, use, rotate and
destroy AES-256 encryption keys. These sample Java applications demonstrate
how to access the KMS API using the Google Java API Client Libraries.

## Prerequisites

### Enable the API

You must [enable the Google Cloud KMS API](https://console.cloud.google.com/flows/enableapi?apiid=cloudkms.googleapis.com) for your project in order to use these samples

### Set Environment Variables

You must set your project ID in order to run the tests

`GOOGLE_CLOUD_PROJECT=<your-project-id-here>`

### Grant Permissions

You must ensure that the [user account or service account](https://cloud.google.com/iam/docs/service-accounts#differences_between_a_service_account_and_a_user_account) you used to authorize your gcloud session has the proper permissions to edit KMS resources for your project. More information can be found in the [Google KMS Docs](https://cloud.google.com/kms/docs/reference/permissions-and-roles)

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

    mvn clean compile assembly:single

You can run the quickstart with:

    java -cp target/kms-samples-1.0.0-jar-with-dependencies.jar \
        com.example.Quickstart [your-project-id] [your-location]

and can see the available snippet commands with:

    java -cp target/kms-samples-1.0.0-jar-with-dependencies.jar \
        com.example.Snippets

For example:

    java -cp target/kms-samples-1.0.0-jar-with-dependencies.jar \
        com.example.Snippets createKeyRing -p [your-project-id] [your-location] myFirstKeyRing
