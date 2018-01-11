## Datastore Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=datastore/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This directory contains sample code used in Google Cloud Datastore documentation. Included here is a sample command line application, `TaskList`, that interacts with Datastore to manage a to-do list.

## Run the `TaskList` sample application.

1. Ensure that you have:
  * Created a Google Developers Console project with the Datastore API enabled. Follow [these instructions](https://cloud.google.com/docs/authentication#preparation) to get your project set up. 
  * Installed the Google Cloud SDK and run the following commands in command line: `gcloud auth application-default login` and `gcloud config set project [YOUR PROJECT ID]`.
  * Installed [Maven](https://maven.apache.org/) and Java 7 (or above).

2. Compile the program by typing `mvn clean compile` in command line.

3. Run the program by typing `mvn exec:java` in command line. In addition to listing tasks via this command line interface, you can view tasks you create in the [Google Cloud Developer's Console](https://console.cloud.google.com/).
