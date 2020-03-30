# Google Analytics sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/analytics/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Integrating App Engine with Google Analytics.

## Project setup, installation, and configuration

- Register for [Google Analytics](http://www.google.com/analytics/), create
an application, and get a tracking Id.
- [Find your tracking Id](https://support.google.com/analytics/answer/1008080?hl=en)
and set it as an environment variable in [`appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml).

## Running locally
This example uses the
[Maven Cloud SDK based plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    $ mvn appengine:run

## Deploying

    $ mvn clean package appengine:deploy
