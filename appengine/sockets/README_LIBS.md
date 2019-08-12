
# Using the Socket API with Some Common Third-Party Libraries

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/sockets/README_LIBS.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Samples for the Java 8 runtime can be found [here](/appengine-java8).

## Using the Socket API with JavaPNS

**Note:** <a href="http://code.google.com/p/javapns/">JavaPNS</a>
is an open source library for Apple Push Notifications. With socket support for
App Engine, many applications can now use JavaPNS on App Engine directly.

App Engine does not officially support JavaPNS, although it may work
with these caveats:

-  **JavaPNS** uses
  <a href="http://code.google.com/p/javapns/source/browse/trunk/src/javapns/communication/ConnectionToAppleServer.java#27">Security.getProperty</a>,
  which was not permitted in versions of App Engine earlier than 1.7.3. However,
  if you need this to work in 1.7.2, you may change the `javapns`
  source to not use `Security.getProperty`, and simply set
  **ALGORITHM** to ``sunx509`. (Future versions are  expected to fix this by replacing the use of
  `Security.getProperty` with  `KeyManagerFactory.getDefaultAlgorithm()`, which will work correctly
  with older versions of App Engine.

- App Engine does not support signed JAR files; accordingly the "Bouncy Castle" jar  provided with the <code>javapns</code> distribution fails to load. To fix this,
remove the <code>META-INF/MANIFEST.MF</code> file from ``bcprov-jdk15-146.jar`` using the following command line:

	zip -d bcprov-jdk15-146.jar META-INF/MANIFEST.MF


## Using JavaMail with the Socket API

App Engine does not officially support JavaMail, although it may work
with these caveats:

<a href="http://www.oracle.com/technetwork/java/javamail/index-138643.html">JavaMail</a>
  1.4.5 is compatible with App Engine but requires a work-around to
  resolve the issue of
  <a href="http://developers.google.com/appengine/docs/java/runtime?hl=en#jar_ordering">class loader
  ordering</a>.  Currently, there are JavaMail classes in the `appengine-api.jar`, which is scanned
  before all other JAR files.  This causes the wrong `javax.mail` classes  to be loaded.  The work-around is to unzip the JavaMail 1.4.5
  `mailapi.jar` file (excluding the `META-INF/MANIFEST.MF` file) into the `WEB-INF/classes` directory. This causes the correct
  classes to be loaded before those in `appengine-api.jar`.

