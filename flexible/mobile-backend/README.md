# Build a mobile app using Firebase and App Engine flexible environment

This tutorial contains Android client sample code for the [Build a Mobile App
Using Firebase and App Engine Flexible
Environment](https://cloud.google.com/solutions/mobile/mobile-firebase-app-engine-flexible)
solution. You can find the sample code for the Android client code in
[firebase-android-client](https://github.com/GoogleCloudPlatform/android-docs-samples/blob/master/firebase-android-client).

## Deployment requirements

- Enable the following services in the [Google Cloud Platform
  console](https://console.cloud.google.com):
  - Google App Engine
  - Google Compute Engine
- Sign up for [Firebase](https://firebase.google.com/) and create a new project
  in the [Firebase console](https://console.firebase.google.com/).
- Install the following tools in your development environment:
  - [Python 2.7](https://www.python.org/downloads/)
  - [Java 8](https://java.com/en/download/)
  - [Apache Maven](https://maven.apache.org/)
  - [Google Cloud SDK](https://cloud.google.com/sdk/)

> **Note**: Firebase is a Google product, independent from Google Cloud
> Platform.

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
1. Select the **Service accounts** tab.
1. Click **Manage all service accounts**.
1. In the **IAM & admin** page click **Create service account**.
1. In the dialog, create an account with the following parameters:
   * Enter *playchat-servlet* in the **Service account name** field.
   * Select **Project** > **Owner** in the **Role** menu.
   * Select **Furnish a new private key**.
   * Choose **JSON** as the key type.
   > **Caution**: The owner role gives the service account full access to all
   > resources in the project. In a production app, you should change the role
   > to the minimum access that your service account requires.
1. After you finish creating the account, your browser downloads the service
   account's private key to your computer as a JSON file. Move the file to the
   `src/main/webapp/WEB-INF` folder in the backend project.
1. In the [Firebase console](https://console.firebase.google.com), select the
   **Database** page. Take note of the URL of the Realtime Database, which is in
   the following format:
   ```
   https://[project-id].firebase.io/
   ```
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

> **Note:** If you run into the following error with the previous command:
>   ```
>   agent library failed to init: instrument
>   ```
>   Update Google Cloud SDK to version `240`:
>   ```
>   gcloud components update --version 240.0.0
>   ```

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
