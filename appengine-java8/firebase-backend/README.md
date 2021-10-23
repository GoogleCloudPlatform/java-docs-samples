# Build a mobile app using Firebase and App Engine flexible environment

![Kokoro Build Status](https://storage.googleapis.com/cloud-devrel-kokoro-resources/java/badges/firebase-appengine-backend.svg)

This repository contains Android client sample code for the [Build a Mobile App
Using Firebase and App Engine Flexible
Environment](https://cloud.google.com/solutions/mobile/mobile-firebase-app-engine-flexible)
solution. You can find the sample code for the Android client code in the
[firebase-android-client](../../../firebase-android-client) repository.

## Deployment requirements

- Enable the following services in the [Google Cloud Platform
  console](https://console.cloud.google.com):
  - Google App Engine
  - Google Compute Engine
- Sign up for [Firebase](https://firebase.google.com/) and create a new project
  in the [Firebase console](https://console.firebase.google.com/).
- Install the following tools in your development environment:
  - [Java 8](https://java.com/en/download/)
  - [Apache Maven](https://maven.apache.org/)
  - [Google Cloud SDK](https://cloud.google.com/sdk/)

> **Note**: Firebase is a Google product, independent from Google Cloud
> Platform.

A Java application deployed to App Engine Flexible Environment [needs to use Java 8 Runtime](https://cloud.google.com/appengine/docs/flexible/java/setting-up-environment). 
However, in your local development environment you can
use JDK 8 or newer as long as your JDK is able to produce Java 8 class files.

## Google Cloud SDK setup

Configure the SDK to access the Google Cloud Platform by using the following
command:

```bash
gcloud auth login
```

Get the project ID from the settings page of your Firebase project. Use the
following command to set your Firebase project as the active project for the
SDK:

```bash
gcloud config set project [project-id]
```

## Configuration

Enable the Google sign-in provider by following these steps:

1. Sign in to the [Firebase console](https://console.firebase.google.com) and
   select your project.
1. In the **Develop** section, select **Authentication**.
1. In the **Authentication** page, select **Sign-in Method**.
1. Select and enable the **Google** sign-in provider.

Follow these steps to configure a service account for the backend application:

1. Go to your project settings page on the [Firebase
   console](https://console.firebase.google.com).
1. Click the **Settings** gear next to 'Project Overview' and then **Project settings**.
1. Select **Service accounts** and click the link **Manage service account permissions**.
1. In the **IAM & admin** page click **Create service account**.
1. In the dialog, create an account with the following parameters:
   * Enter *playchat-servlet* in the **Service account name** field.
   * Select **Project** > **Owner** in the **Role** menu.
   > **Caution**: The owner role gives the service account full access to all
   > resources in the project. In a production app, you should change the role
   > to the minimum access that your service account requires.
1. After the service account is created, click it and choose **Create new key** in the **ADD KEY** dropdown button.
   * Choose **JSON** as the key type.
   * Click **CREATE** to download the key.
1. After you finish creating the account, your browser downloads the service
   account's private key to your computer as a JSON file. Move the file to the
   `src/main/webapp/WEB-INF` folder in the backend project.
1. From the left menu of the [Firebase
   console](https://console.firebase.google.com),
   select **Database** in the **Develop** group.

1. In the **Database** page, click **Create database** in the **Realtime Database** section.

1. In the **Security rules for Realtime Database** dialog, select **Start in
   test mode** and click **Enable**.

    Caution: Test mode allows anyone with your database reference to perform
    read and write operations to your database. If test mode isn't appropriate
    for your purposes, you can write security rules to manage access to your
    data. For more information, see
    [Get Started with Database Rules](https://firebase.google.com/docs/database/security/quickstart)
    in the Firebase documentation.

    This step displays the data you’ve stored in Firebase. In later steps of
    this tutorial, you can revisit this web page to see data added and updated
    by the client app and backend servlet.
1. In the **Rules** tab of the database, make sure you have the security rules for read/write. For example:
    ```json
    {
      "rules": {
        ".read": true,
        ".write": true
      }
    }
    ```
1. Make a note of the Firebase URL for your project, which is in the form
   `https://[project-id].firebaseio.com/` and appears next to a
   link icon.
1. Open the `src/main/webapp/WEB-INF/web.xml` file and do the following:
   * Replace the `JSON_FILE_NAME` placeholder with the JSON file from that
     stores the service account's private key.
   * Replace the `FIREBASE_URL` placeholder with the URL of the Realtime
     Database from the previous step.

   The following example shows the placeholders in the `web.xml` file:
   ```xml
   <init-param>
     <param-name>credential</param-name>
     <param-value>/WEB-INF/JSON_FILE_NAME</param-value>
   </init-param>
   <init-param>
     <param-name>databaseUrl</param-name>
     <param-value>FIREBASE_URL</param-value>
   </init-param>
   ```


## Build and deploy

To build and run the backend module locally:

```bash
mvn clean package appengine:run
```

To deploy the backend module to App Engine:

```bash
mvn clean package appengine:deploy
```

## View user event logs

Run the Android client app, perform some activities such as signing in and
switching channels, and go to the following URL to view user event logs:

```bash
https://[project-id].appspot.com/printLogs
```

## License

Copyright 2018 Google LLC. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS-IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the specific language
governing permissions and limitations under the License.

This is not an official Google product.
