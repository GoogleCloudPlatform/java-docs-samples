# Spring Boot Application Google App Engine Standard with Java 11

This sample shows how to deploy a [Spring Boot](https://spring.io/projects/spring-boot)
application to Google App Engine stadndard.

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
