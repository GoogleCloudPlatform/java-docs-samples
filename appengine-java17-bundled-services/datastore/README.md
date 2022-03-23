# Google Cloud Datastore Sample for App Engine Standard Java17 Bundled Services

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java17-bundled-services/datastore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Google Cloud Datastore][java-datastore]
from [Google App Engine standard Java17 bundled services environment][ae-docs].

[java-datastore]: https://cloud.google.com/appengine/docs/java/datastore/
[ae-docs]: https://cloud.google.com/appengine/docs/standard/java-gen2/services/access

## Difference between App Engine Java8 and Java17 Bundled Services

The only difference between a Java8 application and a Java17 application is in the `appengine-web.xml` file
where you need to define the Java17 runtime and declare you need the App Engine APIs:

```XML
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
    <runtime>java17</runtime>
    <app-engine-apis>true</app-engine-apis>
</appengine-web-app>
```

Everything else should remain the same in terms of App Engine APIs access, WAR project packaging, and deployment.
This way, it should  be easy to migrate your existing GAE Java8 applications to GAE Java17.

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

In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and SOME-VERSION with a valid version number.

```sh
mvn clean package appengine:deploy  -Dapp.deploy.gcloudMode=beta

```
