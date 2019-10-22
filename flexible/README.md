# Google App Engine Flexible Environment Java Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This is a repository that contains Java code samples for [Google App Engine
flexible environment][aeflex-docs].

See our other [Google Cloud Platform GitHub
repos](https://github.com/GoogleCloudPlatform) for sample applications and
scaffolding for other frameworks and use cases.

[aeflex-docs]: https://cloud.google.com/appengine/docs/flexible/

## Getting the sample code

Get the latest sample code from GitHub using Git or download the repository as a ZIP file.
([Download](https://github.com/GoogleCloudPlatform/java-docs-samples/archive/master.zip))

    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git


## Before you begin

1.  Follow the [quickstart for Java in the App Engine flexible
    environment](https://cloud.google.com/appengine/docs/flexible/java/quickstart) to 
    set up your environment to deploy the sample applications App Engine.
    1.  Download and install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/).
    1.  [Install and configure Apache Maven](http://maven.apache.org/index.html).
    1.  [Create a new Google Cloud Platform project, or use an existing
        one](https://console.cloud.google.com/project).
    1.  [Enable billing for your
        project](https://support.google.com/cloud/answer/6293499#enable-billing).
    1. Initialize the Cloud SDK.

           gcloud init

## Deploying to App Engine

To run the application locally, use the Maven Jetty plugin.

    mvn clean jetty:run

View the app at [localhost:8080](http://localhost:8080).

To deploy the app to App Engine, run

    mvn clean appengine:deploy

After the deploy finishes (can take up to 10 minutes), you can view your application at
`https://YOUR_PROJECT.appspot.com`, where `YOUR_PROJECT` is your Google Cloud project ID. You can
see the new version deployed on the [App Engine section of the Google Cloud
Console](https://console.cloud.google.com/appengine/versions).

For a more detailed walkthrough, see the [getting started
guide for Java in the App Engine flexible
environment](https://cloud.google.com/java/getting-started/hello-world).


## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
