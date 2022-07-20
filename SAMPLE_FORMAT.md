# Samples Format

The [Google Cloud Samples Style Guide][style-guide] is considered the primary
guidelines for all Google Cloud samples. This section details some additional,
Java-specific rules that will be merged into the Samples Style Guide in the near
future. 

[style-guide]: https://googlecloudplatform.github.io/samples-style-guide/

## Table of Contents
  * [Java Version](#java-version)
  * [Specific Goals](#specific-goals)
  * [Testing](#testing)
    * [Testing Setup](#testing-setup)
    * [Running Locally](#running-locally)
    * [Gradle Specifics](#gradle-specifics)
    * [Restore System Resources](#restore-system-resources)
  * [Format Guidelines](#format-guidelines)
    * [Location](#project-location)
    * [Dependencies](#project-dependencies)
    * [Configuration](#project-configuration)
    * [Setup](#project-setup)
  * [Code](#code)
    * [Arrange, Act, Assert](#arrange-act-assert)
    * [Style](#style)
    * [Linting](#linting)
    * [Package Names](#package-names)
    * [Class Structure](#class-structure)
    * [Method Comment](#method-comment)
    * [Method Structure](#method-structure)
    * [Exception Handling](#exception-handling)
    * [Client Initialization](#client-initialization)
    * [Command-Line Arguments](#command-line-arguments)
    * [Preferred Dependencies](#preferred-dependencies)
  * [Modern Java](#modern-java)
    * [Lambdas](#lambdas)
    * [Streams](#streams)
    * [Parallel Streams](#parallel-streams)
  * [Additional Best Practices](#additional-best-practices)
    * [Time](#time)
    * [Logging](#logging)

This doc maintains an outline for 'snippet' samples specific to Java. Currently, the Java canonical
samples in this format are located 
[here](https://github.com/googleapis/java-dlp/tree/main/samples/snippets).

Larger sample applications should attempt to follow these guidelines as well, but some may
be ignored or waived as there can be many structural differences between applications and snippets.

## Java Version

New samples should consider using Java 11, but may also support Java 8.

Samples that don't run on either Java 8 or Java 11 should clearly say so in their README and
specifically set the correct Java version in their `pom`. Very rarely will we accept using a
non-LTS JVM version.

### Java 11 features

In general, we do not recommend using the `var` keyword (_technically it's not a keyword, but a
reserved type name_) in API / Client Library samples, unless it's use improves
understanding and readability.  The reviewers call is final on this.

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
Any infrastructure required to run the test (such as a GCS bucket or a Cloud SQL instance) should
be passed in as an environment variable. Tests should clearly indicate which environment variables
are required for the tests to pass.

Resources required outside of this infrastructure should be generated and cleaned up (even on 
failures) as part of the test suite. Please note that tests should run successfully in parallel, 
and UUIDs should be used to prevent conflicts.

Snippets should have integration tests that should verify the snippet compiles and runs
successfully. Tests should only verify that the sample itself is interacting with the service
correctly - it is an explicit non goal for tests to verify that API is performing correctly.
Because of this, mocks for external services are strongly discouraged.

* Test Library: [JUnit4](https://junit.org/junit4/)
* Test Runner: [Maven Failsafe plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) 
(Integration Tests) and [Maven Surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) (Unit Tests).

Most of our sample tests are Integration Tests and should be marked as such using either the Prefix or suffix `IT`.

As an example, the following test code shows how we test a region tag (region tags are tools Google
uses to identify sections of the snippets to be highlighted in documentation) called `region_tag`:
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
public class SomeClassIT {
  @Test public void regionTag_shouldPass() throws Exception { }

  @Test public void regionTag_shouldFail() throws Exception {
    Assert.fail("should fail");
  }
}
```
You will note:
 * underscores (`_`) are used in test method names to separate blocks of `camelCase` text
 * these blocks denote _region tags_, which serve as unique IDs for snippets in a given repository
 * `camelCase` blocks beginning with `should` or `does` are **not** region tags - instead, they
 simply describe the test being run

It is also possible to use annotations to provide info for `region_tag` if you need to do this,
please contact one of the repo admins.

### Testing Setup
Most samples require a GCP project and billing account. Keep the following in
mind when setting up tests.

* **Environment variables**  
  Minimize additional environment variables that need to be set to run the tests.
  If you do require additional environment variables, they should be added to
  [run_tests.sh](../../blob/main/.kokoro/tests/run_tests.sh).

  Existing environment variables include:
  * `GOOGLE_APPLICATION_CREDENTIALS`
  * `GOOGLE_CLOUD_PROJECT`
  * `PROJECT_ID`

* **API library**  
  If an API needs to be enabled in the testing project, add this information to the
  directory's README and to the comments in the PR. If there is no README.md file, add one
  in your PR.

* **IAM**
  Some API's require that the service account have some additional capibilities. These should also
  be mentioned in both the PR and the README.

* **Cloud resources**  
  Most Java samples create the Cloud resources that they need to run. If this
  is resource intensive or not possible, add instructions to the directory's README.md file
  to add the resource to the testing project. Tests that create cloud resources should also delete
  those resources when they are done testing in a way that ensures the deletion
  of the resource even if the test fails, such as with with a `finally` block or in an `@After`
  or `@AfterClass` function.  Also, resources should not used fixed names, but prefer UUID's as
  we have many tests that run at the same time.

* **Keys and Secrets**
  Add a note in the pull request, in order for a Java maintainer to assist you
  in adding keys and secrets to the testing project.
  
### Running Locally
Run tests locally with commands:

* Maven: `mvn verify`
* Gradle: `gradle build test`

To run the `functions` tests (or other tests without a parent `pom.xml`), use the following command:

```
cd functions
find */pom.xml | xargs -I {} echo $(pwd)/{} | xargs -I {} dirname {} | xargs -I {} sh -c "cd {} && mvn clean verify"
```

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

### Restore System resources
Ideally, saving and restoring `System.out` and `System.err` should be done by Junit Rules - we don't yet have that facility in place.  In the mean time,
it's important that if you capture `System.out` in a `@Before` method that you save it, and then restore it later in an `@After` method. (If you don't do 
this, or `setOut` to `null` it may cause problems for other tests. (they won't see output when debugging)


## Format Guidelines
### Project Location
Samples should be in a project folder under the name of the technology the snippet represents. 
Additional subfolders should be used to differentiate groups of samples. Execution technologies,
like Compute, Cloud Run, Dataproc, Dataflow, Functions may have subfolder's for other technologies to
show using the two technologies together.

Folder and package paths should try to avoid containing unnecessary folders to allow users to more
easily navigate to the snippets themselves. However, it is encouraged to use common names like
"snippets" and "quickstart" to allow users to more easily discover the project contents.

For example, the the `java-docs-samples/compute` folder may have the following projects:
- `compute/snippets`
- `compute/quickstart`
  
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
Some frameworks such as Spring require the `parent` atom to be used. If this applies to you,
contact the maintainers for guidance. 

When adding a dependency to a GCP client library, the 
[libraries-bom](https://github.com/GoogleCloudPlatform/cloud-opensource-java/wiki/The-Google-Cloud-Platform-Libraries-BOM)
should be used instead of explicitly declaring the client version. See the below example:
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
  printing the contents of the response to `stdout`.

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
To run the Checkstyle, ErrorProne and SpotBugs plugins on an existing sample, run

```shell
mvn clean verify -DskipTests -P lint
```

The `-DskipTests` is optional. It is useful if you want to verify that your code
builds and adheres to the style guide without waiting for tests to complete.

### Package Names
Samples should use package names in the following formats:

  `<PRODUCT>`, `<PRODUCT1>.<PRODUCT2>`, `<PRODUCT>.<IDEA>` is preferred; fewer levels are preferred.
Legacy samples that use `com.example.<YOUR_PRODUCT>.<FOLDER>`, such as `com.example.dlp.snippets` or 
`com.example.functions.snippets` are still accepted and do not need to migrate to the preferred system (above).

  **NEVER** use `com.google.*` for samples or snippets. Use of the default package is **strongly** discouraged.

### Class Structure
Each snippet should be be contained in its own file, within a class with a name descriptive of the
snippet and a similarly named method. Region tags should start below the `package` (if there is 
one), but should include the class and any imports in full. Additional methods can be used if it
improves readability of the sample.

```java
// [START product_example]
import com.example.resource;

public class exampleSnippet {
  // Snippet methods ...
}
// [END product_example]
```
### Method Comment
Include a short, descriptive comment detailing what action the snippet it attempting to perform.
Avoid using the javadoc format, as these samples are not used to generate documentation and it can
be redundant.
```java
// This is an example snippet for showing best practices.
public static void exampleSnippet(String projectId, String filePath) {
    // Snippet content ...
}
```
### Method Structure
Method arguments should be limited to what is absolutely required for testing (ideally having at
most 4 arguments). In most cases, this is project specific information or the path to an external
file. For example, project specific information (such as `projectId`) or a `filePath` for an
external file is acceptable, while an argument for the type of a file or a specific action is not.
 
Any declared function arguments should include a no-arg, main method with examples for how
the user can initialize the method arguments and call the entrypoint for the snippet. If the
values for these variables need to be replaced by the user, be explicit that
they are example values only.

Snippet methods should specify a return type of `void` and avoid returning any value wherever
possible. Instead, show the user how to interact with a returned object programmatically by printing
some example attributes to the console. 
```java
public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String filePath = "path/to/image.png";
    inspectImageFile(projectId, filePath);
}

// This is an example snippet for showing best practices.
public static void exampleSnippet(String projectId, String filePath) {
    // Snippet content ...
}
```
### Exception Handling
Samples should include examples and details of how to catch and handle common `Exceptions` that are
the result of improper interactions with the client or service. Lower level exceptions that are 
the result of environment or hardware errors (such as `IOException`, `InteruptedException`, or 
`FileNotFoundException`) should be allowed to bubble up to the next level.

If there is no solution (or if the solution is too verbose to resolve in a sample) to resolving the
exception programmatically (such as a missing resource), it is acceptable to either log or leave a
comment clearly explaining what actions the user should take to correct the situation.

In general, follow the 
[Google Java style guide](https://google.github.io/styleguide/javaguide.html#s6.2-caught-exceptions)
and catch the most specific type of `Exception`, instead of a more general one.

Example:
```java
try {
  // Do something
} catch (IllegalArgumentException ok) {
  // IllegalArgumentException's are thrown when an invalid argument has been passed to a function. Ok to ignore.
}
```

For example, looking at the code in googleapis/java-dialogflow searching for `throws` and `catch`, I see lots of specific enhanced exceptions - our samples should reflect the richness of those. For example, [ApiException](http://googleapis.github.io/gax-java/1.7.1/apidocs/com/google/api/gax/rpc/ApiException.html) has 16 known subclasses that Gax throws. gRPC also throws [`io.grpc.StatusException`](https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusException.html) which has additional info to help developers understand the cause of their errors. There is also [`io.grpc.StatusRuntimeException`](https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusRuntimeException.html) and [`io.grpc.ManagedChannelProvider.ProviderNotFoundException`](https://grpc.github.io/grpc-java/javadoc/io/grpc/ManagedChannelProvider.ProviderNotFoundException.html).  By listing them explicitly, users are clued into looking them up to understand how the API works and what might happen in production. 


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
### Command-Line Arguments
**NOTE:** Snippet should be optimized to run directly from a user's IDE. Command-Line arguments are
considered an anti-pattern, and new samples should not implement them. 

**Dataflow** samples are an exception to this guideline.

### Preferred Dependencies
Google written dependencies are always preferred over alternatives.  For example:
  * [Guava](https://github.com/Google/Guava) is preferred over [Apache commons lang](https://commons.apache.org/)
  * [GSON](https://github.com/Google/GSON) is preferred for JSON parsing.
  * [Google HTTP Client](https://github.com/googleapis/google-http-java-client) is preferred.

## Modern Java
Prefer using modern idioms / language features over older styles.

### Lambdas
Should be about 1-3 lines at most, otherwise it should be in a named method.
* Prefer lambdas to anonymous classes
  
### Streams
Streams can be extremely compact, efficient, and easy to use - consider using them.
* Avoid side effects (changes outside the scope of the stream)
* Prefer `for` each loops to `.foreach()`
* Checked Exceptions can be problematic inside streams.
  
### Parallel Streams
Parallel Streams make sense in a few situations. However, there are many situations where their use is a
net loss.  Really think through your usage and consider what they might mean if you are already
doing concurrent operations.

## Additional Best Practices 
The following are some general Java best practices that should be followed in samples to remain
idiomatic. 

### Time
Use the `java.time` package when dealing with units of time in some manner.

### Logging
Use [slf4j](http://www.slf4j.org/) as shown [here](https://cloud.google.com/logging/docs/setup/java#example) for consistent logging. Unless you are demonstrating how
to use raw Stackdriver API's.
