# Samples Format
## Table of Contents
  * [Java Version](#java-version)
  * [Specific Goals](#specific-goals)
  * [Testing](#testing)
    * [Testing Setup](#testing-setup)
    * [Running Locally](#running-locally)
    * [Gradle Specifics](#gradle-specifics)
  * [Format Guidelines](#format-guidelines)
    * [Location](#project-location)
    * [Dependencies](#project-dependencies)
    * [Configuration](#project-configuration)
    * [Setup](#project-setup)
  * [Code](#code)
    * [Arrange, Act, Assert](#arrange-act-assert)
    * [Style](#style)
    * [Linting](#linting)
    * [Command-Line Arguments](#command-line-arguments)
    * [Package Names](#package-names)
    * [Class Structure]()
    * [Function Comment]()
    * [Function Structure]()
    * [Exception Handling]()
    * [Client Initialization](#client-initialization)
  * [Modern Java](#modern-java)
    * [Lambdas](#lambdas)
    * [Streams](#streams)
    * [Parallel Streams](#parallel-streams)
  * [Additional Best Practices](#additional-best-practices)
    * [Time](#time)
    * [Logging](#logging)

This doc maintains an outline for 'snippet' samples specific to Java. Currently, the java canonical
samples in this format are located 
[here](/tree/master/dlp/src/main/java/dlp/snippets).

Larger sample applications should attempt to follow many of these guidelines as well, but some may
be ignored or waived as there can be many structural differences between applications and snippets.

## Java Version

All samples should be written to run on both Java 8 and Java 11, samples that don't run on Java 8 
should clearly says so in their README and specifically set Java 11 in their `pom`.  There should be
a clear reason why Java 8 isn't supported.

## Specific Goals
This sample format is intended to help enforce some specific goals in our samples. Even if not 
specifically mentioned in the format, samples should make best-effort attempts in the following:

* __Copy-paste-runnable__ - Users should be able to copy and paste the code into their own
  environments and run with as few and transparent modifications as possible. Samples should be as
  easy for a user to run as possible.
  
* __Teach through code__ - samples should teach users both how and why specific best practices
  should be implemented and performed when interacting with our services.
  
* __Idiomatic__ - examples should make best attempts to remain idiomatic and encourage good 
  practices that are specific to a language or practice. 

## Build Tools

All new samples should build and run integration tests with [Maven](https://maven.apache.org/).
[Gradle](https://gradle.org/) support is optional as we don't yet have regular testing.

## Testing
Snippets should have integration tests that should verify the snippet works and compiles correctly.
Creating mocks for these tests are optional. These tests should capture output created by the
snippet to verify that it works correctly. See the tests in the canonical for an example of how to
do this correctly. 

* Test Library: [JUnit4](https://junit.org/junit4/)
* Test Runner: [Maven Failsafe plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/)
and [Maven Surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/).

As an example, the following test code shows how we test a region tag called `region_tag`:
```java
package com.google.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Store region_tag in test method name, camel-cased
 */ 
@RunWith(JUnit4.class)
public class SomeClassTest {
  @Test public void regionTag_shouldPass() throws Exception { }

  @Test public void regionTag_shouldFail() throws Exception {
    Assert.fail("should fail");
  }
}
```
You will note that the "_" in `region_tag` is removed, and "_" is used to separate regionTags from
test descriptions.

It is also possible to use annotations to provide info for `region_tag` if you need to do this,
please contact one of the repo admins.

### Testing Setup
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
  If an API needs to be enabled in the testing project, add this information to the
  directory's README and to the comments in the PR. If there is no README.md file, add one
  in your PR.

* **IAM**
  Some API's require that the service account have some additional capibilities, these should also
  be mentioned in both the PR and the README.

* **Cloud resources**  
  Most Java samples create the Cloud resources that they need to run. If this
  is resource intensive or not possible, add instructions to the directory's CONTRIBUTING.md file
  to add the resource to the testing project.

* **Keys and Secrets**
  Add a note in the pull request, in order for a Java maintainer to assist you
  in adding keys and secrets to the testing project.
  
### Running Locally
Run tests locally with commands:

* Maven: `mvn verify`
* Gradle: `gradle build test`

### Gradle Specifics
#### NEEDS WORK
Your `build.gradle` should have the following section:
```groovy
test {
  useJUnit()
  systemProperty 'cucumber.options', '--plugin junit:target/surefire-reports/cucumber-junit.xml' 
  include '**/*Test.class'
}
```
<!--
test {
  useJUnit()
  testLogging.showStandardStreams = true
  beforeTest { descriptor -\>
     logger.lifecycle("test: " + descriptor + "  Running")
  }

  onOutput { descriptor, event -\>
     logger.lifecycle("test: " + descriptor + ": " + event.message )
  }
  afterTest { descriptor, result -\>
    logger.lifecycle("test: " + descriptor + ": " + result )
  }
}
-->

## Format Guidelines
### Project Location
Samples should be in a project folder under the name of the technology the snippet represents. 
Additional sub folders should be used to differentiate groups of samples. Execution technologies,
like Compute, CloudRun, Dataproc, Dataflow, functions may have subfolders for other technologies to
show using the two technologies together.

Folder and package paths should try to avoid containing unnecessary folders to allow users to more
easily navigate to the snippets themselves. 
  
### Project Dependencies
A Maven project should have a `pom.xml` that is formatted and easily human readable that declares
a parent pom as shown below, and declares all dependencies needed for the project. Best attempts
should be made to minimize necessary dependencies without sacrificing the idiomatic practices. 
```xml
  <!--
    The parent pom defines common style checks and testing strategies for our samples.
    Removing or replacing it should not affect the execution of the samples in anyway.
  -->
  <parent>
    <groupId>com.google.cloud.samples</groupId>
    <artifactId>shared-configuration</artifactId>
    <version>SPECIFY_LATEST_VERSION</version>
  </parent>
```
Some frameworks (such as Spring) require the `parent` atom to be used, if this applies to you,
contact the maintainers for guidance. 

When adding a dependency to a GCP client library, the 
[libraries-bom](https://github.com/GoogleCloudPlatform/cloud-opensource-java/wiki/The-Google-Cloud-Platform-Libraries-BOM)
should ideally be used instead of explicitly declaring the client version. See the below example:
```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>com.google.cloud</groupId>
        <artifactId>libraries-bom</artifactId>
        <version>SPECIFY_LATEST_VERSION</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>com.google.cloud</groupId>
      <artifactId>google-cloud-storage</artifactId>
    </dependency>
  </dependencies>
```

### Project Configuration
Use of environment variables over system properties is strongly preferred for configuration.

Any additional files required should be stored in `src/test/resources`.

### Project Setup
The README.md should contain instructions for the user to get the samples operable. Ideally, steps
  such as project or resource setup should be links to Cloud Documentation. This is to reduce 
  duplicate instructions and README maintenance in the future. 

# Code
### Arrange, Act, Assert
Samples should generally follow the "Arrange, Act, Assert" outline to: 
* _Arrange_ - Create and configure the components for the request. Avoid nesting these components,
  as complex, nested builders can be hard to read.
* _Act_ - Send the request and receive the response.
* _Assert_ - Verify the call was successful or that the response is correct. This is often done by
  print contents of the response to `stdout`.

## Style
Samples in this repository follow the [Google Java Style Guide][java-style].
This is enforced by [Checkstyle](https://checkstyle.org/) and the
[Maven Checkstyle Plugin][checkstyle-plugin].

[java-style]: https://google.github.io/styleguide/javaguide.html
[checkstyle-plugin]: https://maven.apache.org/plugins/maven-checkstyle-plugin/

Use the [google-java-format][google-java-format] tool to automatically reformat
your source code to adhere to the style guide. It is available as a command-line
tool or IntelliJ plugin.

[google-java-format]: https://github.com/google/google-java-format

### Linting
To run the checkstyle & ErrorProne plugins on an existing sample, run

```shell
mvn clean verify -DskipTests
```

The `-DskipTests` is optional. It is useful if you want to verify that your code
builds and adheres to the style guide without waiting for tests to complete.

### Command-Line Arguments
Simple command-line samples with only positional arguments should use the
`args` argument to `main(String... args)` directly. A command-line sample
which has optional parameters should use the [Apache Commons
CLI](https://commons.apache.org/proper/commons-cli/index.html) library.

**Dataflow** samples are an exception to this guideline, since Dataflow has its own 
[method for setting options](https://cloud.google.com/dataflow/pipelines/specifying-exec-params).

### Package Names
It is not necessary to use a package for snippets.  If you choose to use one, please DO NOT use 
`com.google.` as a prefix.  `com.example` or something related to the technology such as
`package dlp.snippets`.

### Class Structure
Each snippet should be be contained in its own file, within a class with a name descriptive of the
snippet and a similarly named function. Region tags should start below the `package` (if there is 
one), but should include the class and any imports in full. Additional functions can be used if it improves
readability of the sample.

```java
// [START product_example]
import com.example.resource;

public class exampleSnippet {
  // Snippet functions ...
}
// [END product_example]
```
### Function Comment
Include a short, descriptive comment detailing what action the snippet it attempting to perform.
Avoid using the javadoc format, as these samples are not used to generate documentation and it can
be redundant.
```java
// This is an example snippet for show best practices.
public static void exampleSnippet(String projectId, String filePath) {
    // Snippet content ...
}
```
### Function Structure
Function parameters should be limited to what is absolutely required for testing (ideally having at
most 4 parameters). In more cases,
this is project specific information or the path to an external file. For example, project specific
information (such as `projectId`) or a `filePath` for an external file is acceptable, while a
parameter for the type of a file or a specific action is not.
 
Any declared function parameters should include a no-arg, overloaded function with examples for how
the user can initialize the function parameters and call the entrypoint for the snippet. If the
values for these variables need to be replaced by the user, attempt to make it explicitly clear that
they are example values only.

Snippet functions should specific a return type of `void` and avoid returning any value wherever
possible. Instead, show the user how to interact with a returned object programmatically by printing
some example attributes to the console. 
```java
public static void exampleSnippet() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String filePath = "path/to/image.png";
    inspectImageFile(projectId, filePath);
}

// This is an example snippet for show best practices.
public static void exampleSnippet(String projectId, String filePath) {
    // Snippet content ...
}
```
### Exception Handling
Snippets should follow the
[Google Java style guide](https://google.github.io/styleguide/javaguide.html#s6.2-caught-exceptions)
and catch the most specific type of `Exception`, instead of a more general one. Additionally, 
exceptions of any try/catch blocks should be limited to where an error can actually (within reason)
occur. Ideally, we will provide either code or comments suggesting how the developer can mitigate
the exception in the catch block, or why it's safe to ignore.

If their is no solution (or if the solution is too verbose to resolve inside the snippet) then include `throws`
and a list of exceptions in the method definition and don't catch the exception.  Though it would be
helpful to users if you mentioned that it might occur.

Example:
```java
try {
  // Do something
} catch (IllegalArgumentException ok) {
  // IllegalArgumentException's are thrown when an invalid argument has been passed to a function. Ok to ignore.
}
```
### Client Initialization
The preferred style for initialization is to use a try-with-resources statement with a comment 
clarifying how to handle multiple requests and clean up instructions. 

Example:
```java
// Initialize client that will be used to send requests. This client only needs to be created
// once, and can be reused for multiple requests. After completing all of your requests, call
// the "close" method on the client to safely clean up any remaining background resources.
try (DlpServiceClient dlp = DlpServiceClient.create()) {
  // Do something
}
```
## Modern Java
Prefer using modern idioms / language features over older styles.

### Lambdas
Should be about 1-3 lines at most, otherwise it should be in a named method.
* Prefer lambdas to annonymous classes
  
### Streams
Streams can be extremely compact, efficient, and easy to use - consider using them.
* Avoid side effects (changes outside the scope of the stream)
* Prefer `for` each loops to `.foreach()`
* Checked Exceptions can be problematic inside streams.
  
### Parallel Streams
Parallel Streams make make sense in a few situations. There are many situations where there use is a
net loss.  Really think through your usage and consider what they might mean if you are already
doing concurrent operations.

## Additional Best Practices 
The following are some general Java best practices that should be followed in samples to remain
idiomatic. 

### Time
Use the `java.time` package when dealing with units of time in some manner. 

### Logging
Use `java.util.logging` for consistent logging in web applications. Unless you are demonstrating how
to use Stackdriver Logging.
