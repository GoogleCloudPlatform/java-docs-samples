# Spring Boot Application Google App Engine Standard with Java 11

This sample shows how to deploy a Spring Boot application with an exploded fatjar
to Google App Engine, using the `entrypoint` element in the [app.yaml](app.yaml)
to start your application. The sample uses the `java` command is used to compile
and execute the Java source code.

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
