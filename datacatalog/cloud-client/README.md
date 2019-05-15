# Getting Started with Data Catalog and the Google Cloud Client libraries

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=datacatalog/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Data Catalog][datacatalog] is a fully managed and scalable metadata management service that empowers organizations
to quickly discover, manage, and understand all their data in Google Cloud.
This sample Java application demonstrates how to access the Data Catalog API using
the [Google Cloud Client Library for Java][google-cloud-java].

[datacatalog]: https://cloud.google.com/data-catalog/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

#### Setup
- Install [Maven](http://maven.apache.org/).
- [Enable](https://console.cloud.google.com/apis/api/datacatalog.googleapis.com/overview) Data Catalog API.
- Set up [authentication](https://cloud.google.com/docs/authentication/getting-started).

#### Build
- Build your project with:
```
  mvn clean package -DskipTests
```

#### Testing
Run the test with Maven.
```
  mvn verify
```
