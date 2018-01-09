# Users Authentication sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/logs/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use the [Logs API][log-docs] on [Google App
Engine][ae-docs].

[log-docs]: https://cloud.google.com/appengine/docs/java/logs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally

The Logs API only generates output for deployed apps, so this program should not be run locally.

## Deploying

This example uses the
[Cloud SDK maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).

    mvn appengine:deploy
