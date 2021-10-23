# Cloud Identity & Access Management Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=iam/api-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Identity & Access Management](https://cloud.google.com/iam/) (IAM)
lets administrators authorize who can take action on specific resources.
These sample applications demonstrate how to interact with Cloud IAM using
the Google API Client Library for Java.

## Quickstart

The Quickstart does the following: 

*  Initializes the Resource Manager service, which manages GCP projects.
* Reads the [IAM policy](https://cloud.google.com/iam/docs/overview#cloud-iam-policy)
  for your project.
* Modifies the IAM policy by granting the Log Writer role
  (`roles/logging.logWriter`) to your Google Account.
* Writes the updated IAM policy.
*  Prints all the members in your project that have the Log Writer role
   (`roles/logging.logWriter`).
*  Revokes the Log Writer role.

To build and run the Quickstart, install [Maven](http://maven.apache.org/).

To build the project, run the following command:

```xml
mvn clean package
```

To run the Quickstart, ensure that the Resource Manager API is enabled
for your project and that you have set up authentication. For details, see the
[Before you begin](https://cloud.google.com/iam/docs/quickstart-client-libraries#before-you-begin)
section of the IAM client library Quickstart documentation.

Then, replace the `projectId` and `member` fields with your
project ID and member ID, and run the following command:

```xml
mvn exec:java
```

For more information, see the [IAM client library Quickstart documentation](https://cloud.google.com/iam/docs/quickstart-client-libraries).