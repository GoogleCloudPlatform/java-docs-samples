# Standalone HTTP Server on Google App Engine Standard with Java 11

This sample shows how to deploy an application to Google App Engine using the
a fat jar. There is no `entrypoint` field listed in the [`app.yaml`](src/main/appengine/app.yaml),
as the application is a single fat jar with the correct MainClass field in the MANIFEST.

## Setup

See [Prerequisites](../README.md#Prerequisites).

## Deploy to App Engine Standard

```
mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

To view your app, use command:
```
gcloud app browse
```
Or navigate to http://<project-id>.appspot.com URL.
