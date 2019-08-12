# Multitenancy Java sample

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/multitenancy/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

Shows the usage of the Namespaces API.

An App Engine guestbook using Java, Maven, and Objectify.

Data access using [Objectify](https://github.com/objectify/objectify)

Please ask questions on [Stackoverflow](http://stackoverflow.com/questions/tagged/google-app-engine)

## Running Locally

How do I, as a developer, start working on the project?

1. `mvn clean appengine:devserver`

## Deploying

1. `mvn clean appengine:update -Dappengine.appId=PROJECT -Dappengine.version=VERSION`
