# Google Model Armor

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=modelarmor/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Google Model Armor is a fully managed Google Cloud service that enhances the security and safety of AI applications by screening LLM prompts and responses for various security and safety risks. [More Details](https://cloud.google.com/security-command-center/docs/model-armor-overview)

These sample Java code snippets demonstrate how to access the Model Armor API using the Google Java API Client Libraries.

## Prerequisites

### Enable the API

The following page details the permissions required for Model Armor and provides instructions for enabling and disabling Model Armor:
[Enable Model Armor API](https://cloud.google.com/security-command-center/docs/get-started-model-armor#enable-model-armor)

### Grant Permissions
You must ensure that the [user account or service account](https://cloud.google.com/iam/docs/service-accounts#differences_between_a_service_account_and_a_user_account) you used to authorize your gcloud session has the proper permissions to edit Secret Manager resources for your project. In the Cloud Console under IAM, add the following roles to the project whose service account you're using to test:
* Model Armor Admin (roles/modelarmor.admin)
* Floor Settings Admin (modelarmor.floorSettingsAdmin)

More information can be found in the [Model Armor Docs](https://cloud.google.com/security-command-center/docs/get-started-model-armor#required_permissions)

### Set Environment Variables

You must set your project ID to run the tests:
```shell
export GOOGLE_CLOUD_PROJECT=<your-project-id-here>
```