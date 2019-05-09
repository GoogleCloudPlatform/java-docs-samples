# Micronaut Application on Google App Engine Standard with Java 11

This sample shows how to deploy a [Micronaut](https://micronaut.io/index.html)
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
