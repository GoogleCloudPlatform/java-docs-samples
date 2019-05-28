# Using a Custom Entrypoint on Google App Engine Standard with Java 11

This sample shows how to deploy an application to Google App Engine, using the
`entrypoint` element in the [app.yaml](app.yaml) to start your application. The
sample uses the `java` command is used to compile and execute the Java source code.

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
