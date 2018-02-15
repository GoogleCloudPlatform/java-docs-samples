Bigtable-hello-j8
=================

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/bigtable/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

Moves the Bigtable Hello World application to Google App Engine Standard for Java 8.


* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.3.9)
* [Gradle](https://gradle.org)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

Initialize the Google Cloud SDK using:

    gcloud init

    gcloud auth application-default login

Then you need to [Create a Cloud Bigtable Instance](https://cloud.google.com/bigtable/docs/creating-instance)


## Using Maven

### Run Locally

    mvn -Dbigtable.projectID=PROJECTID -Dbigtable.instanceID=INSTANCEID appengine:run

### Deploy to App Engine Standard for Java 8

    mvn -Dbigtable.projectID=PROJECTID -Dbigtable.instanceID=INSTANCEID appengine:deploy

### Run Integration Tests

    mvn -Dbigtable.projectID=PROJECTID -Dbigtable.instanceID=INSTANCEID verify

## Using Gradle

### Run Locally

    gradle -Dbigtable.projectID=PROJECTID -Dbigtable.instanceID=INSTANCEID appengineRun

### Integration Tests & Deploy to App Engine Standard for Java 8

    gradle -Dbigtable.projectID=PROJECTID -Dbigtable.instanceID=INSTANCEID appengineDeploy

As you add / modify the source code (`src/main/java/...`) it's very useful to add
[unit testing](https://cloud.google.com/appengine/docs/java/tools/localunittesting)
to (`src/main/test/...`).  The following resources are quite useful:

* [JUnit4](http://junit.org/junit4/)
* [Mockito](http://mockito.org/)
* [Truth](http://google.github.io/truth/)

### When done

Cloud Bigtable Instances should be [deleted](https://cloud.google.com/bigtable/docs/deleting-instance)
when they are no longer being used as they use significant resources.
