# Google App Engine Standard Environment Images Sample

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/images/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use the Images Java API.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Modify the app

Using the [Google Cloud SDK](https://cloud.google.com/sdk/) create a bucket

    $ gsutil mb YOUR-PROJECT-ID.appspot.com

* Edit `src/main/java/com/example/appengine/images/ImageServlet.java` and set your `bucket` name.

## Running locally

 This example uses the
 [App Engine maven plugin](https://cloud.google.com/appengine/docs/java/tools/maven).
 To run this sample locally:

     $ mvn appengine:devserver

 To see the results of the sample application, open
 [localhost:8080](http://localhost:8080) in a web browser.


## Deploying

 In the following command, replace YOUR-PROJECT-ID with your
 [Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
 and SOME-VERSION with a valid version number.

     $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION
