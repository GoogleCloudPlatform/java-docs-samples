# Cloud Talent Solution client samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=jobs/cjd_sample/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Cloud Talent Solution][jobs] is part of Google for Jobs - a Google-wide commitment to help people find jobs more easily. Talent Solution provides plug and play access to Google’s search and machine learning capabilities, enabling the entire recruiting ecosystem - company career sites, job boards, applicant tracking systems, and staffing agencies to improve job site engagement and candidate conversion.

This sample Java application demonstrates how to access the Cloud Talent Solution API using the [Google Cloud Client Library for Java][google-cloud-java].

[jobs]: https://cloud.google.com/talent-solution/job-search/docs
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Setup

1. Install [Maven](http://maven.apache.org/).
1. [Enable](https://console.cloud.google.com/apis/api/jobs.googleapis.com/overview) Cloud Talent Solution API.

## Build

Build your project with:
```
   mvn clean package
```

## Local testing

1. [Create a service account](https://cloud.google.com/docs/authentication/getting-started#creating_the_service_account)
and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, for example:
```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/key.json
```
2. Run all samples with command:
```
   mvn -Dtest=SampleTests test
```
Or a single sample:
```
   mvn exec:java -Dexec.mainClass="com.google.samples.BatchOperationSample"
```
