# Appengine Helloworld sample for Google App Engine Flexible

This sample is used as part of the [Quickstart for Java in the App Engine Flexible Environment](https://cloud.google.com/java/getting-started/hello-world)

## Setup

Use either:

* `gcloud init`
* `gcloud beta auth application-default login`

## Maven
### Running locally

    $ mvn clean jetty:run-exploded
  
### Deploying

    $ mvn appengine:deploy

## Gradle
### Running locally

    $ gradle jettyRun

If you do not have gradle installed, you can run using `./gradlew appengineRun`.

### Deploying

    $ gradle appengineDeploy

If you do not have gradle installed, you can deploy using `./gradlew appengineDeploy`.
