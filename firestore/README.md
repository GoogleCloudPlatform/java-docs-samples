# Getting started with Google Cloud Firestore

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=firestore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Firestore](https://cloud.google.com/firestore/docs/) is a hosted NoSQL database built
for automatic scaling, high performance, and ease of application development.

These code samples demonstrate how to access the Google Cloud Firestore API
using the Beta version of the Firestore Client Libraries.

Note: You cannot use both Cloud Firestore and Cloud Datastore in the
same project, which might affect apps using App Engine. Try using Cloud Firestore with a different
project.

## Setup
- Install [Maven](http://maven.apache.org/).
- Open the [Firebase Console](https://console.firebase.com) and click **Add project**.
- Select the option to **Enable Cloud Firestore Beta** for this project.
- Click **Create Project**.
  When you create a Cloud Firestore project, it also enables the API in the
  [Cloud API Manager](https://console.cloud.google.com/projectselector/apis/api/firestore.googleapis.com/overview).
- [Create a service account](https://cloud.google.com/docs/authentication/)
 and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to the
 credentials JSON file.

## Build
Build your project:

     mvn clean package


## Quickstart
[Quickstart.java](src/main/java/com/example/java/com/example/firestore/Quicstart.java)
 demonstrates adding and querying documents in a collection in Firestore.
You can run the quickstart with:

    mvn exec:java -Dexec.mainClass=com.example.firestore.Quickstart -Dexec.args="your-firestore-project-id"

Note: the default project-id will be used if no argument is provided.

## Snippets
These [code samples](src/main/java/com/example/firestore/snippets) support
the Firestore [documentation](https://cloud.google.com/firestore/docs).

## Tests
Run all tests:
```
   mvn clean verify
```

