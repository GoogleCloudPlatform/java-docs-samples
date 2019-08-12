# Tic Tac Toe on Google App Engine Standard using Firebase
<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/firebase-tictactoe/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

Samples for the Java 8 runtime can be found [here](/appengine-java8).

This directory contains a project that implements a realtime two-player game of
Tic Tac Toe on Google [App Engine Standard][standard], using the [Firebase] database
for realtime notifications when the board changes.

[Firebase]: https://firebase.google.com
[standard]: https://cloud.google.com/appengine/docs/about-the-standard-environment

## Setup

* Install [Apache Maven][maven] 3.0.5 or later
* Create a project in the [Firebase Console][fb-console]
* Download and install the [Cloud SDK](https://cloud.google.com/sdk/)

Initialize the `gcloud` configuration to use your new project:
```
   gcloud init
```
* In the [Overview section][fb-overview] of the Firebase console, click 'Add
  Firebase to your web app' and replace the contents of the file
  `src/main/webapp/WEB-INF/view/firebase_config.jspf` with that code snippet.
* [Enable the Identity API](https://console.cloud.google.com/apis/api/identitytoolkit.googleapis.com/overview)
* If you haven't already, Create an App Engine app within the current Google Cloud Project
```
    gcloud app create
```
* Setup [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials)
to run your application locally:
Download [service account credentials][creds] and set the `GOOGLE_APPLICATION_CREDENTIALS`
environment variable to its path:
```
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials.json
```

[fb-console]: https://console.firebase.google.com
[sdk]: https://cloud.google.com/sdk
[creds]: https://console.firebase.google.com/iam-admin/serviceaccounts/project?project=_&consoleReturnUrl=https:%2F%2Fconsole.firebase.google.com%2Fproject%2F_%2Fsettings%2Fgeneral%2F
[fb-overview]: https://console.firebase.google.com/project/_/overview
[maven]: https://maven.apache.org

## Running locally

    $ mvn appengine:run

When running locally, the page does not automatically refresh,
please reload the page manually after each move.

## Deploying to App Engine Standard

    $ mvn appengine:deploy

## Contributing changes

See [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Licensing

See [LICENSE](../../LICENSE).

