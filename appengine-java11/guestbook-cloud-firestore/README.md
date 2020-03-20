# Guestbook sample for Google App Engine Java 11

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11/guestbook/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to handle form data using
[Cloud Firestore](https://cloud.google.com/firestore/) on Google App Engine
Standard.

This sample also uses packages from [Guava](https://github.com/google/guava),
which provides some basic utility libraries and collections from Google's core
libraries.

## Setup your Google Cloud Platform Project

* Download and initialize the [Cloud SDK](https://cloud.google.com/sdk/)

    `gcloud init`

* If this is your first time creating an App Engine application:
```
   gcloud app create
```

## Setup the Sample App

- Copy the sample apps to your local machine:
```
  git clone https://github.com/GoogleCloudPlatform/java-docs-samples
```

- Add the [appengine-simple-jetty-main](../README.md#appengine-simple-jetty-main)
Main class to your classpath:
```
  cd java-docs-samples/appengine-java11/appengine-simple-jetty-main
  mvn install
```

- Move into the `appengine-java11/guestbook-cloud-firestore` directory and compile the app:
```
  cd ../guestbook-cloud-firestore
  mvn package
```

* Setup [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials) by
[creating a service account](https://cloud.google.com/docs/authentication/production#creating_a_service_account) and downloading the JSON key file.

  * Provide authentication credentials to your application code by setting the
    environment variable `GOOGLE_APPLICATION_CREDENTIALS` to the path of your
    JSON key file.

    `export GOOGLE_APPLICATION_CREDENTIALS="[PATH]"`

* Replace `YOUR-PROJECT-ID` in [`Persistence.java`](/src/main/java/com/example/guestbook/Persistence.java)  with your project Id you created.

* Create a [Cloud Firestore in Native mode](https://cloud.google.com/firestore/docs/firestore-or-datastore) database by going to the
[Cloud Firestore UI](https://console.cloud.google.com/firestore/data) and
from the Select a database service screen:
  * Choose Cloud Firestore in Native mode.
  * Select a Cloud Firestore location.
  * Click Create Database.

**Cloud Firestore and App Engine:** You can't use both Cloud Firestore and Cloud Datastore in the same project, which might affect apps using App Engine. Try using Cloud Firestore with a different project if you need to use Cloud Datastore.

## Deploying

Deploy your application using the maven plugin:

```
mvn clean package appengine:deploy
```

View your application:
```
gcloud app browse
```
or by visiting `https://<your-project-id>.appspot.com`.
