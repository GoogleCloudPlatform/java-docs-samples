# Deployment Manager Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=deployment-manager/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Deployment Manager](https://cloud.google.com/deployment-manager/docs/) is an infrastructure deployment service that automates the creation and management of Google Cloud Platform resources. Write flexible template and configuration files and use them to create deployments that have a variety of Cloud Platform services, such as Google Cloud Storage, Google Compute Engine, and Google Cloud SQL, configured to work together.

## Authentication

GCP client libraries use a strategy called Application Default Credentials (ADC) to find your application's credentials. When your code uses a client library, the strategy checks for your credentials in the following order:

 - First, ADC checks to see if the environment variable GOOGLE_APPLICATION_CREDENTIALS is set. If the variable is set, ADC uses the service account file that the variable points to. The next section describes how to set the environment variable.
 - If the environment variable isn't set, ADC uses the default service account that Compute Engine, Kubernetes Engine, App Engine, and Cloud Functions provide, for applications that run on those services.
 - If ADC can't use either of the above credentials, an error occurs.

Run `gcloud auth application-default login` to configure application credential file locally.

See the [documentation](https://cloud.google.com/docs/authentication/production#finding_credentials_automatically) for more details.

## Quickstart

Install [Maven](http://maven.apache.org/).

Compile 

```bash
mvn clean 
mvn compile
```

Set the environment variable `GOOGLE_PROJECT_ID` 
```bash
export GOOGLE_PROJECT_ID=<yourprojectid>
```

Run the Deployment Manager Demo, which lists deployments and either updates or inserts a new deployment based on name. The deployment template is located in `yaml/deployment-manager-config.yaml` 

```bash
mvn exec:java
```
