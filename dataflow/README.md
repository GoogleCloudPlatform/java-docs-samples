# Getting started with Google Cloud Dataflow

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dataflow/README.md)

[Apache Beam](https://beam.apache.org/)
is an open source, unified model for defining both batch and streaming
data-parallel processing pipelines.
This guides you through all the steps needed to run an Apache Beam pipeline in the
[Google Cloud Dataflow](https://cloud.google.com/dataflow) runner.

## Setting up your Google Cloud project

The following instructions help you prepare your Google Cloud project.

1. Install the [Cloud SDK](https://cloud.google.com/sdk/docs/).

   > ℹ️ This is not required in
   > [Cloud Shell](https://console.cloud.google.com/cloudshell/editor)
   > since it already has the Cloud SDK pre-installed.

1. Create a new Google Cloud project via the
    [*New Project* page](https://console.cloud.google.com/projectcreate),
    or via the `gcloud` command line tool.

    ```sh
    export PROJECT=your-google-cloud-project-id
    gcloud projects create $PROJECT
    ```

1. Setup the Cloud SDK to your GCP project.

    ```sh
    gcloud init
    ```

1. [Enable billing](https://cloud.google.com/billing/docs/how-to/modify-project).

1. [Enable the Dataflow API](https://console.cloud.google.com/flows/enableapi?apiid=dataflow).

1. Create a service account JSON key via the
    [*Create service account key* page](https://console.cloud.google.com/apis/credentials/serviceaccountkey).

    ```sh
    export PROJECT=$(gcloud config get-value project)
    export SA_NAME=samples
    export IAM_ACCOUNT=$SA_NAME@$PROJECT.iam.gserviceaccount.com

    # Create the service account.
    gcloud iam service-accounts create $SA_NAME --display-name $SA_NAME

    # Set the role to Project Owner (*).
    gcloud projects add-iam-policy-binding $PROJECT \
      --member serviceAccount:$IAM_ACCOUNT \
      --role roles/owner

    # Create a JSON file with the service account credentials.
    export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/credentials.json
    gcloud iam service-accounts keys create $GOOGLE_APPLICATION_CREDENTIALS \
      --iam-account=$IAM_ACCOUNT
    ```

    > ℹ️ The **Role** field authorizes your service account to access resources.
    > You can view and change this field later by using the
    > [GCP Console IAM page](https://console.cloud.google.com/iam-admin/iam).
    > If you are developing a production app,
    > specify more granular permissions than `roles/owner`.
    >
    > To learn more about roles in service accounts, see
    > [Granting roles to service accounts](https://cloud.google.com/iam/docs/granting-roles-to-service-accounts).

    To learn more about service accounts, see
    [Creating and managing service accounts](https://cloud.google.com/iam/docs/creating-managing-service-accounts)

1. Set the `GOOGLE_APPLICATION_CREDENTIALS` to your service account key file.

    ```sh
    export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/credentials.json
    ```

## Setting up a Java development environment

The following instructions help you prepare your development environment.

1. Download and install the
    [Java Development Kit](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=openj9).
    Verify that the
    [JAVA_HOME](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/envvars001.html)
    environment variable is set and points to your JDK installation.

    ```sh
    $JAVA_HOME/bin/java --version
    ```

1. Download and install
    [Apache Maven](http://maven.apache.org/download.cgi)
    by following the
    [Maven installation guide](http://maven.apache.org/install.html)
    for your specific operating system.

    ```sh
    mvn --version
    ```

1. *(Optional)* Set up an IDE like
    [IntelliJ](https://www.jetbrains.com/idea/),
    [VS Code](https://code.visualstudio.com),
    [Eclipse](https://www.eclipse.org/ide/).
    [NetBeans](https://netbeans.org),
    etc.

## *(Optional)* Create a new Apache Beam pipeline

The easiest way to create a new Apache Beam pipeline is through the starter
Maven archetype.

```sh
export NAME=your-pipeline-name
export PACKAGE=org.apache.beam.samples
export JAVA_VERSION=11

# This creates a new directory with the pipeline's code within it.
mvn archetype:generate \
    -DarchetypeGroupId=org.apache.beam \
    -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-starter \
    -DtargetPlatform=$JAVA_VERSION \
    -DartifactId=$NAME \
    -DgroupId=$PACKAGE \
    -DinteractiveMode=false

# Navigate to the pipeline contents.
cd $NAME
```

Make sure you have the latest plugin and dependency versions,
and update your `pom.xml` file accordingly.

```sh
# Check your plugin versions.
mvn versions:display-plugin-updates

# Check your dependency versions.
mvn versions:display-dependency-updates
```

Finally, add the runners or I/O transforms you need into your `pom.xml` file.
