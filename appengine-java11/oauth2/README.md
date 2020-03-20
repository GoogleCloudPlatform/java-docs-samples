# Google OAuth 2.0 on Google App Engine Standard with Java 11

This sample shows how to implement an OAuth 2.0 flow using the
[Google OAuth Client Library for Java][client] to access protected data stored
on Google services. OAuth 2.0 is a standard specification for allowing end
users to securely authorize a client application to access protected
server-side resources.

[client]: https://developers.google.com/api-client-library/java/google-api-java-client/oauth2

## Setup you Google Cloud Project

- Make sure [`gcloud`](https://cloud.google.com/sdk/docs/) is installed and initialized:
```
   gcloud init
```
- If this is the first time you are creating an App Engine project
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
- In the [Cloud Developers Console](https://cloud.google.com/console) >
API Manager > Credentials, create a OAuth Client ID for a Web Application.
You will need to provide an authorized redirect URI
origin: `https://<PROJECT_ID>.appspot.com/oauth2callback`.

- Replace `CLIENT_ID` and `CLIENT_SECRET` with these values in your
[app.yaml](/src/main/appengine/app.yaml)

- Move into the `appengine-java11/oauth2` directory and compile the app:
```
  cd ../oauth2
  mvn package
```

## Deploy to App Engine Standard

```
mvn clean package appengine:deploy
```

To view your app, use command:
```
gcloud app browse
```
Or navigate to `https://<your-project-id>.appspot.com`.
