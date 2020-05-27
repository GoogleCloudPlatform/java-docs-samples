# Standalone HTTP Server on Google App Engine Standard with Java 11

This sample shows how to deploy an application to Google App Engine from source. The `entrypoint` field listed in the [`app.yaml`](src/main/appengine/app.yaml) is not required,
as GAE will determine the entrypoint by searching the `target` directory for the .jar file with a Main-Class Manifest entry.

## Setup

See [Prerequisites](../README.md#Prerequisites).

## Deploy to App Engine Standard

```
gcloud app deploy
```

To view your app, use command:
```
gcloud app browse
```
Or navigate to `https://<your-project-id>.appspot.com`.
