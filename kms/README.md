# Google Cloud KMS

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=kms/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Cloud KMS](https://cloud.google.com/kms/) is a cloud-hosted key
management service for encrypting, decrypting, signing, and verifying data.
These sample Java applications demonstrate how to access the Cloud KMS API using the
Google Java API Client Libraries.

## Prerequisites

### Enable the API

You must [enable the Google Cloud KMS API](https://console.cloud.google.com/flows/enableapi?apiid=cloudkms.googleapis.com) for your project in order to use these samples

### Set Environment Variables

You must set your project ID in order to run the tests

```
$ export GOOGLE_CLOUD_PROJECT="<your-project-id-here>"
```

### Grant Permissions

You must ensure that the [user account or service account](https://cloud.google.com/iam/docs/service-accounts#differences_between_a_service_account_and_a_user_account) you used to authorize your gcloud session has the proper permissions to edit KMS resources for your project. In the Cloud Console under IAM, add the following roles to the project whose service account you're using to test:

* Cloud KMS Admin
* Cloud KMS CryptoKey Encrypter/Decrypter
* Cloud KMS Importer
* Cloud KMS CryptoKey Public Key Viewer
* Cloud KMS CryptoKey Signer/Verifier

More information can be found in the [Google KMS Docs](https://cloud.google.com/kms/docs/reference/permissions-and-roles)
