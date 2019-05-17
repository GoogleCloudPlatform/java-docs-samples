# Deployment Manager Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=deployment-manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Deployment Manager] (https://cloud.google.com/deployment-manager/docs/) is an infrastructure deployment service that automates the creation and management of Google Cloud Platform resources. Write flexible template and configuration files and use them to create deployments that have a variety of Cloud Platform services, such as Google Cloud Storage, Google Compute Engine, and Google Cloud SQL, configured to work together.

## Quickstart

 - Authentication uses the GOOGLE_APPLICATION_CREDENTIALS environment variable.
 - Run `gcloud auth application-default login` to create/update the file locally
 - The default path on macos is `GOOGLE_APPLICATION_CREDENTIALS=/Users/[username]/.config/gcloud/application_default_credentials.json`
 - In intellij you can set this environment variable in the runtime config next to the run button. 
 - javadoc reference for deployment manger is [here](https://developers.google.com/resources/api-libraries/documentation/deploymentmanager/v2/java/latest/)



Install [Maven](http://maven.apache.org/).

Build 

```xml
mvn clean
```

Compile 

```xml
mvn compile
```

Run the Deployment Manager Demo, which lists deployments and either updates or inserts a new deployment based on name.

```xml
mvn exec:java
```