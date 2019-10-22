# Verify instance identity Java sample for Google Compute Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=compute/signed-metadata/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This repository contains example code in Java that downloads and verifies JWT
provided by metadata endpoint, which is available on Compute Engine instances in
GCP.

More about this verification can be read on official Google Cloud documentation
[Veryfying the Identity of
Instances](https://cloud.google.com/compute/docs/instances/verifying-instance-identity).

## Running on Compute Engine

To run the sample, you will need to do the following:

1. Create a compute instance on the Google Cloud Platform Developer's Console
1. SSH into the instance you created
1. Update packages and install required packages

    `sudo apt-get update && sudo apt-get install git-core openjdk-8-jdk maven`

1. Clone the repo

    `git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git`

1. Navigate to `java-docs-samples/compute/signed-metadata/`

    `cd java-docs-samples/compute/signed-metadata/`

1. Use maven to package the class as a jar

    `mvn clean package`

1. Make sure that openjdk 8 is the selected java version

    `sudo update-alternatives --config java`

1. Execute the jar file

    `java -jar target/compute-signed-metadata-1.0-SNAPSHOT-jar-with-dependencies.jar`
