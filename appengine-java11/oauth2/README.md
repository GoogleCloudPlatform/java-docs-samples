# Google OAuth 2.0 on Google App Engine Standard with Java 11

This sample shows how to implement an OAuth 2.0 flow using the
[Google OAuth Client Library for Java](client) to access protected data stored
on Google services. OAuth 2.0 is a standard specification for allowing end
users to securely authorize a client application to access protected
server-side resources.

[client]: https://developers.google.com/api-client-library/java/google-api-java-client/oauth2

## Setup

1. See [Prerequisites](../README.md#Prerequisites).
1. In the [Cloud Developers Console](https://cloud.google.com/console) >
API Manager > Credentials, create a OAuth Client ID for a Web Application.
You will need to provide an authorized redirect URI
origin: `https://<PROJECT_ID>.appspot.com/oauth2callback`.
1. Replace `CLIENT_ID` and `CLIENT_SECRET` with these values in your
[app.yaml](/src/main/appengine/app.yaml)

## Deploy to App Engine Standard

```
mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

To view your app, use command:
```
gcloud app browse
```
Or navigate to http://<project-id>.appspot.com URL.
