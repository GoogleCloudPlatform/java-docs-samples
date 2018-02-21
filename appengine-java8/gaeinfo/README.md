# Google App Engine Information

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/gaeinfo/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample displays what's going on in your app. It dumps the environment and lots more.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

Use either:

* `gcloud init`
* `gcloud auth application-default login`

## Maven
### Running locally

    $ mvn appengine:run

### Deploying

    $ mvn appengine:deploy

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
