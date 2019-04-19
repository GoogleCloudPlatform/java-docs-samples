# Guestbook sample for Google App Engine Java 11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/guestbook/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to handle form data using
[Cloud Firestore](https://cloud.google.com/firestore/) on Google App Engine
Standard.

## Setup

* Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If this is your first time creating an App Engine application:
```
   gcloud app create
```

* Setup [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)

    `gcloud auth application-default login`

* Replace `your-project-id` in [`Persistence.java`](/src/main/java/com/example/guestbook/Persistence.java)  with your project Id you created.

## Deploying

Deploy your application using the maven plugin:

```
mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

View your application:
```
gcloud app browse
```
or by visiting `https://<your-project-id>.appspot.com`.
