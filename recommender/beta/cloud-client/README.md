# Getting Started with Recommender and the Google Java API Client library

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=README.md&cloudshell_working_dir=recommender/beta/cloud-client/">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Cloud Recommender](https://cloud.google.com/recommender/) is a service on Google Cloud that provides 
usage recommendations for Cloud products and services. This sample Java 
application demonstrates how to access the Recommender API using the 
[Google Cloud Client Library for Java](https://github.com/GoogleCloudPlatform/google-cloud-java).

## Quickstart

### Setup
- [Set up a Java Development Environment for Maven](https://cloud.google.com/java/docs/setup).
- [Enable Recommender API](https://cloud.google.com/recommender/docs/enabling) for your project.
- [Authenticate using a service account](https://cloud.google.com/docs/authentication/getting-started).
Create a service account, download a JSON key file, and set the 
`GOOGLE_APPLICATION_CREDENTIALS` environment variable.
- Set the `GOOGLE_CLOUD_PROJECT` environment variable to your project ID.

### Build
Build your project with:
```
mvn clean package -DskipTests
```
		 
## Testing
To run the unit tests:
```
mvn clean verify
```
