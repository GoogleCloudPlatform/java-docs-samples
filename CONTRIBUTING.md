# How to become a contributor and submit your own code

* [Contributor License Agreements](#Contributor-License-Agreements)
* [Contributing a Patch or New Sample](#Contributing-a-Patch)
* [Build Tools](#build-tools)
* [Integration Testing](#testing)
* [Style](#Style)

## Contributor License Agreements

We'd love to accept your sample apps and patches! Before we can take them, we
have to jump a couple of legal hurdles.

Please fill out either the individual or corporate Contributor License Agreement
(CLA).

  * If you are an individual writing original source code and you're sure you
    own the intellectual property, then you'll need to sign an [individual
    CLA](https://developers.google.com/open-source/cla/individual).
  * If you work for a company that wants to allow you to contribute your work,
    then you'll need to sign a [corporate
    CLA](https://developers.google.com/open-source/cla/corporate).

Follow either of the two links above to access the appropriate CLA and
instructions for how to sign and return it. Once we receive it, we'll be able to
accept your pull requests.

## Contributing a Patch or New Sample

1. Sign a [Contributor License Agreement](#Contributor-License-Agreements).
1. Set up your [Java Developer Environment](https://cloud.google.com/java/docs/setup).
1. Fork the repo.
1. Develop and test your code.
1. Ensure that your code adheres to the [SAMPLE_FORMAT.md](SAMPLE_FORMAT.md)
guidelines.
1. Ensure that your code has an appropriate set of unit tests which all pass.
1. Submit a pull request.
1. A maintainer will review the pull request and make comments.

## Build Tools

 All new samples should build and run integration tests with both [Maven](https://maven.apache.org/) and [Gradle](https://gradle.org/).

## Integration Testing

All samples must have Integration Tests that run with Maven and Gradle

* Test Library: [JUnit4](https://junit.org/junit4/)
* Test Runner: [Maven Failsafe plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) and [Maven Surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/).

### Running Tests Locally

Run tests locally with commands:
* Maven: `mvn verify`
* Gradle: `gradle build test`


### Gradle Specifcs
Your `build.gradle` should have the following section:

```groovy

test {
  useJUnit()
  testLogging.showStandardStreams = true
  beforeTest { descriptor ->
     logger.lifecycle("test: " + descriptor + "  Running")
  }

  onOutput { descriptor, event ->
     logger.lifecycle("test: " + descriptor + ": " + event.message )
  }
  afterTest { descriptor, result ->
    logger.lifecycle("test: " + descriptor + ": " + result )
  }
}
```

### Other Testing Set Up

Most samples require a GCP project and billing account. Keep the following in
mind when setting up tests.

* **Environment variables**  
  Minimize additional environment variables that need to be set to run the tests.
  If you do require additional environment variables, they should be added to
  `run_tests.sh`.

  Existing environment variables include:
  * `GOOGLE_APPLICATION_CREDENTIALS`
  * `GOOGLE_CLOUD_PROJECT`
  * `PROJECT_ID`


* **API library**  
  Add a note in the pull request, if an API needs to be enable in the testing
  project.

* **Cloud resources**  
  Most Java samples create the Cloud resources that they need to run. If this
  is resource intensive or not possible, add a note in the pull request for the
  resource to be added to the testing project.

* **Keys and Secrets**
  Add a note in the pull request, in order for a Java maintainer to assist you
  in adding keys and secrets to the testing project.

## Style

Samples in this repository follow the [Google Java Style Guide][java-style].
This is enforced using the [Maven Checkstyle Plugin][checkstyle-plugin].

[java-style]: https://google.github.io/styleguide/javaguide.html
[checkstyle-plugin]: https://maven.apache.org/plugins/maven-checkstyle-plugin/

Use the [google-java-format][google-java-format] tool to automatically reformat
your source code to adhere to the style guide. It is available as a command-line
tool or IntelliJ plugin.

[google-java-format]: https://github.com/google/google-java-format

### Adding the Checkstyle Plugin to New Samples

The samples in this repository use a common parent POM to define plugins used
for linting and testing. Add the following to your sample POM to ensure that it
uses the common Checkstyle configuration. For more information, see the
[java-repo-tools](https://github.com/GoogleCloudPlatform/java-repo-tools)
repository.

```xml
<!--
    The parent pom defines common style checks and testing strategies for our samples.
    Removing or replacing it should not affect the execution of the samples in anyway.
  -->
<parent>
  <groupId>com.google.cloud</groupId>
  <artifactId>doc-samples</artifactId>
  <version>1.0.11</version>
</parent>
```

### Running the Linter

To run the checkstyle & ErrorProne plugins on an existing sample, run

```shell
mvn clean verify -DskipTests
```

The `-DskipTests` is optional. It is useful if you want to verify that your code
builds and adheres to the style guide without waiting for tests to complete.


### Parsing Command-Line Arguments in Samples

Simple command-line samples with only positional arguments should use the
`args` argument to `main(String... args)` directly. A command-line sample
which has optional parameters should use the [Apache Commons
CLI](https://commons.apache.org/proper/commons-cli/index.html) library.

Dataflow samples are an exception to this rule, since Dataflow has [its own
method for setting custom
options](https://cloud.google.com/dataflow/pipelines/specifying-exec-params).
