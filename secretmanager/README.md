# Google Secret Manager

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=secretmanager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google [Secret Manager](https://cloud.google.com/secret-manager/) provides a
secure and convenient tool for storing API keys, passwords, certificates and
other sensitive data. These sample Java applications demonstrate how to access
the Secret Manager API using the Google Java API Client Libraries.

## Prerequisites

### Enable the API

You must [enable the Secret Manager API](https://console.cloud.google.com/flows/enableapi?apiid=secretmanager.googleapis.com) for your project in order to use these samples

### Set Environment Variables

You must set your project ID in order to run the tests

```text
$ export GOOGLE_CLOUD_PROJECT=<your-project-id-here>
```

### Grant Permissions

You must ensure that the [user account or service account](https://cloud.google.com/iam/docs/service-accounts#differences_between_a_service_account_and_a_user_account) you used to authorize your gcloud session has the proper permissions to edit Secret Manager resources for your project. In the Cloud Console under IAM, add the following roles to the project whose service account you're using to test:

* Secret Manager Admin (`roles/secretmanager.admin`)
* Secret Manager Secret Accessor (`roles/secretmanager.secretAccessor`)

More information can be found in the [Secret Manager Docs](https://cloud.google.com/secret-manager/docs/access-control)
