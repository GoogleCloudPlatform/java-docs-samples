# JavaMail API Email Sample for Google App Engine Standard Environment

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/  <!-- Parent POM defines ${appengine.sdk.version} (updates frequently). -->
  <parent>
    <groupId>com.google.cloud</groupId>
    <artifactId>appengine-doc-samples</artifactId>
    <version>1.0.0</version>
    <relativePath>..</relativePath>
  </parent>
/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use [JavaMail][javamail-api] on [Google App Engine
standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/
[javamail-api]: http://javamail.java.net/
[sample-docs]: https://cloud.google.com/appengine/docs/java/mail/

## Setup
1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.

## Running locally
    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update
