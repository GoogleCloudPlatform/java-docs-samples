# A Java Task Queue example for Google App Engine

This sample demonstrates how to use the [TaskQueue API][taskqueue-api] on [Google App
Engine][ae-docs].

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/taskqueue/push-samples/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


[taskqueue-api]: https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/package-summary
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally 
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/managed-vms/maven).
To run this sample locally:

    $ mvn appengine:devserver

Go to the site `localhost:8080` to add elements to the queue.  They will appear in the log as the result of the Enqueue servlet transmitting the data to the Worker servlet.

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://support.google.com/cloud/answer/6158840).

    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION

## Setup
To save your project settings so that you don't need to enter the
 parameters, you can:

1. Update the `<application>` tag in src/main/webapp/WEB-INF/appengine-web.xml
   with your project name.

2. Update the `<version>` tag in src/main/webapp/WEB-INF/appengine-web.xml
   with a valid version number.


You will now be able to run

    $ mvn appengine:update

without the need for any additional parameters.
