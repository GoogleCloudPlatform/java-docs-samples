# Ktor Application on Google App Engine Standard with Kotlin

This sample shows how to deploy a [Ktor](https://ktor.io/)
application to Google App Engine standard.

## Setup

See [Prerequisites](../README.md#Prerequisites).

## Deploying

```bash
 mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

To view your app, use command:
```
gcloud app browse
```
Or navigate to http://<project-id>.appspot.com URL.
