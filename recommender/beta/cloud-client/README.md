# Getting Started with Recommender and the Google Java API Client library

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=README.md&cloudshell_working_dir=recommender/beta/cloud-client/">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Cloud Recommender](https://cloud.google.com/recommender/) is a service on Google Cloud that provides 
usage recommendations for Cloud products and services. This sample Java 
application demonstrates how to access the Recommender API using the 
[Google Cloud Client Library for Java](https://github.com/GoogleCloudPlatform/google-cloud-java).

## Quickstart

### Setup
- Install [Maven](http://maven.apache.org/).
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
	
### List Recommendations
To list recommendations for your project:
```
mvn exec:java -Dexec.mainClass=com.example.recommender.ListRecommendations \
    -Dexec.args="location-id recommender-id"
```
		
`location-id` is the [Google Cloud location](https://cloud.google.com/compute/docs/regions-zones/)
where resources associated with the recommendations are located and 
`recommender-id` is the fully-qualified [recommender ID](https://cloud.google.com/recommender/docs/recommenders#recommenders).

Press `Ctrl+C` to exit the application.
		
To list IAM recommendations for your project:
```
mvn exec:java -Dexec.mainClass=com.example.recommender.ListRecommendations \
    -Dexec.args="global google.iam.policy.Recommender"
```   
## Testing
To run the unit tests:
```
mvn clean verify
```
