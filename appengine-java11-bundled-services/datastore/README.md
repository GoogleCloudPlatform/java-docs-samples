# Google Cloud Datastore Sample for App Engine Standard Java11 Bundled Services

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java11-bundled-services/datastore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Google Cloud Datastore][java-datastore]
from [Google App Engine standard Java11 bundled services environment][ae-docs].

[java-datastore]: https://cloud.google.com/appengine/docs/java/datastore/
[ae-docs]: https://cloud.google.com/appengine/docs/standard/java11/services/access

## Difference between App Engine Java8 and Java11 Bundled Services

The only difference between a Java8 application and a Java11 application is in the `appengine-web.xml` file
where you need to define the Java11 runtime and declare you need the App Engine APIs:

```XML
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
    <runtime>java11</runtime>
    <app-engine-apis>true</app-engine-apis>
</appengine-web-app>
```

Everything else should remain the same in terms of App Engine APIs access, WAR project packaging, and deployment.
This way, it should  be easy to migrate your existing GAE Java8 applications to GAE Java11.

## Running locally

This example uses the
[Cloud SDK Maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

```sh
mvn package appengine:run
```
To see the results of the sample application, open
[localhost:8080](http://localhost:8080) in a web browser.


## Deploying

```sh
mvn clean package appengine:deploy
mvn appengine:deployIndex
```
