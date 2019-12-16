# Samples Format

This doc maintains an outline for 'snippet' samples specific to Java. Currently, the java canonical
samples in this format are located 
[here](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/dlp/src/main/java/dlp/snippets).

Larger sample applications should attempt to follow many of these guidelines as well, but some may
be ignored or waived as there can be many structural differences between applications and snippets.

## Java Version

All samples should be written to run on both Java 8 and Java 11, samples that don't run on Java 8 should clearly says so in their README and disable testing on Java 8.  There should be a clear reason why Java 8 isn't supported.

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

## Format Guidelines

### Project Location
Samples should be in a project folder under the name of the product the snippet represents. 
  Additional sub folders should be used to differentiate groups of samples. Folder and package paths
  should try to avoid containing unnecessary folders to allow users to more easily navigate to the
  snippets themselves. 
  
### Project Dependencies
Project should have a `pom.xml` that is readably formatted, declares a parent pom as shown below,
 and declares all dependencies needed for the project. Best attempts should be made to minimize
 necessary dependencies without sacrificing the idiomatic practices. 

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

When adding a dependency to a GCP client library, the [libraries-bom](https://github.com/GoogleCloudPlatform/cloud-opensource-java/wiki/The-Google-Cloud-Platform-Libraries-BOM)
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

### Class Structure
Each snippet should be be contained in its own file, within a class with a name descriptive of the
snippet and a similarly named function. Region tags should start below the package, but should
include the class and any imports in full. Additional functions can be used if it improves
readability of the sample.


```java
package dlp.snippets;

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
be redundant whe

```java
// This is an example snippet for show best practices.
public static void exampleSnippet(String projectId, String filePath) {
    // Snippet content ...
}
```
  
### Function Structure
Function parameters should be limited to what is absolutely required for testing. In more cases,
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
and catch the most specific type of `Exception`, instead of a more general one. Additionally, exceptions
of any try/catch blocks should be limited to where an error can actually (within reason) occur. Ideally, we will
provide either code or comments suggesting how the developer can mitigate the exception in the catch block, or
why it's safe to ignore.

If their is no solution (or if the solution is too verbose to resolve inside the snippet) then include `throws`
and a list of exceptions in the method definition and either don't catch the exception or catch and rethrow it.

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
} catch (IOException e) {
  System.out.println("Unable to intailize service client, as a network error occured: \n" 
    + e.toString());
}
```

### Arrange, Act, Assert
Samples should generally follow the "Arrange, Act, Assert" outline to: 
* _Arrange_ - Create and configure the components for the request. Avoid nesting these components,
  as complex, nested builders can be hard to read.
* _Act_ - Send the request and receive the response.
* _Assert_ - Verify the call was successful or that the response is correct. This is often done by
  print contents of the response to `stdout`.

### Testing
Snippets should have tests that should verify the snippet works and compiles correctly. Creating 
 mocks for these tests are optional. These tests should capture output created by the snippet to 
 verify that it works correctly. See the tests in the canonical for an example of how to do this
 correctly. 

## Additional Best Practices 

The following are some general Java best practices that should be followed in samples to remain
idiomatic. 

### Modern Java
Prefer using modern ideoms / language features over older styles.

* Lambda's should be about 1-3 lines at most, otherwise it should be in a named method.
  * Prefer to annonymous classes
* Streams can be extremely compact, efficient, and easy to use - consider using them.
  * Avoid side effects
  * Prefer for each loops to `.foreach`
  * Checked Exceptions can be problematic inside streams.
* Parallel Streams make make sense in some situations, but there are many situations where there use is a net loss.  Really think through your usage and consider what they might mean if you are already doing concurrent operations.

### Style
Wherever possible (and when not conflicting any of the above guidelines), follow the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). It's encouraged, but
not required to use `[google-java-format](https://github.com/google/google-java-format)` to help 
format your code. 

### Time
Use the `java.time` package when dealing with units of time in some manner. 

### Logging
Use `java.util.logging` for consistent logging in web applications. 
