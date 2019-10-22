# Getting Started with Google Cloud Natural Language API and the Google Cloud Client libraries

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=language/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Cloud Natural Language API][language] provides natural language
understanding technologies to developers, including sentiment analysis, entity
recognition, and syntax analysis. This API is part of the larger collection of
Cloud Machine Learning APIs.

These sample Java applications demonstrate how to access the Cloud Natural
Language API using the [Google Cloud Client Library for Java][google-cloud-java].

[language]: https://cloud.google.com/natural-language/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

```bash
mvn clean compile assembly:single
```

### Analyze a string for sentiment (using the quickstart sample)

```
java -cp target/language-google-cloud-samples-1.0.9-jar-with-dependencies.jar \
    com.example.language.QuickstartSample
```
