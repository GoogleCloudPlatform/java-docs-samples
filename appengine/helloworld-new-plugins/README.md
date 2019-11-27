# Google App Engine Standard Environment Hello World Sample

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/helloworld-new-plugins/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to deploy an application on Google App Engine.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

Use either:

* `gcloud init`
* `gcloud beta auth application-default login`

## Maven
### Running locally

    $ mvn appengine:run

### Deploying

    $ mvn appengine:deploy

## Gradle
### Running locally

    $ gradle appengineRun

If you do not have gradle installed, you can run using `./gradlew appengineRun`.

### Deploying

    $ gradle appengineDeploy

If you do not have gradle installed, you can deploy using `./gradlew appengineDeploy`.

<!--
## Intelij Idea
Limitations - Appengine Standard support in the Intellij plugin is only available for the Ultimate Edition of Idea.

### Install and Set Up Cloud Tools for IntelliJ

To use Cloud Tools for IntelliJ, you must first install IntelliJ IDEA Ultimate edition.

Next, install the plugins from the IntelliJ IDEA Plugin Repository.

To install the plugins:

1. From inside IDEA, open File > Settings (on Mac OS X, open IntelliJ IDEA > Preferences).
1. In the left-hand pane, select Plugins.
1. Click Browse repositories.
1. In the dialog that opens, select Google Cloud Tools.
1. Click Install.
1. Click Close.
1. Click OK in the Settings dialog.
1. Click Restart (or you can click Postpone, but the plugins will not be available until you do restart IDEA.)

### Running locally

### Deploying
 -->
