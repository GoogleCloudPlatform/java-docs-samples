# How to become a contributor and submit your own code

* [Contributor License Agreements](#Contributor-License-Agreements)
* [Contributing a Patch](#Contributing-a-Patch)
* [Contributing a new sample](#Contributing-a-new-sample)
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

## Contributing a Patch

1. Submit an issue describing your proposed change to the repo in question.
1. The repo owner will respond to your issue promptly.
1. If your proposed change is accepted, and you haven't already done so, sign a
   Contributor License Agreement (see details above).
1. Fork the desired repo, develop and test your code changes.
1. Ensure that your code adheres to the existing style in the sample to which
   you are contributing.
1. Ensure that your code has an appropriate set of unit tests which all pass.
1. Submit a pull request.

## Contributing a new sample

1. App Engine Standard samples all go into `/appengine` (java 7) or `/java8-appengine` (Java 8) (if you're contributing a group of samples,
please put the App Engine Standard sample into `/appengine` and provide a link in both `README.md`'s for
the project for the additional sample.

1. App Engine Flexible samples all go into `/flexible`

1. Technology samples go into the project root.


## Build Tools

For instructions regarding development environment setup, please visit [the documentation](https://cloud.google.com/java/docs/setup). All new samples should build and run integration tests with both [Maven](https://maven.apache.org/) and [Gradle](https://gradle.org/).

## Testing

All samples must have Integration Tests (ie. They need to run against a real service) that run with
`mvn verify` & `gradle build test`.  If we need to enable an API, let us know.

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

### Keys and Secrets

Please contact a Java DPE for instructions before adding to Travis.

## Style

Samples in this repository follow the [Google Java Style Guide][java-style].
This is enforced using the [Maven Checkstyle Plugin][checkstyle-plugin].

[java-style]: https://google.github.io/styleguide/javaguide.html
[checkstyle-plugin]: https://maven.apache.org/plugins/maven-checkstyle-plugin/

Use the [google-java-format][google-java-format] tool to automatically reformat
your source code to adhere to the style guide. It is available as a command-line
tool or IntelliJ plugin.

[google-java-format]: https://github.com/google/google-java-format

### Running the Linter

To run the checkstyle & ErrorProne plugins on an existing sample, run

```shell
mvn clean verify -DskipTests
```

The `-DskipTests` is optional. It is useful if you want to verify that your code
builds and adheres to the style guide without waiting for tests to complete.

### Adding the Checkstyle Plugin to New Samples

The samples in this repository use a common parent POM to define plugins used
for linting and testing. Add the following to your sample POM to ensure that it
uses the common Checkstyle configuration.

```xml
<parent>
  <groupId>com.google.cloud</groupId>
  <artifactId>doc-samples</artifactId>
  <version>1.0.0</version>
  <!-- Change relativePath to point to the root directory. -->
  <relativePath>../..</relativePath>
</parent>
```

This is just used for testing. The sample should build without a parent defined.

### Parsing Command-Line Arguments in Samples

Simple command-line samples with only positional arguments should use the
`args` argument to `main(String... args)` directly. A command-line sample
which has optional parameters should use the [Apache Commons
CLI](https://commons.apache.org/proper/commons-cli/index.html) library.

Dataflow samples are an exception to this rule, since Dataflow has [its own
method for setting custom
options](https://cloud.google.com/dataflow/pipelines/specifying-exec-params)

