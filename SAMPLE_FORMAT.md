# Samples Format

This doc maintains an outline for 'snippet' samples specific to Java. Currently, the java canonical
samples in this format are located 
[here](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/dlp/src/main/java/dlp/snippets).


## Specific Goals
This sample format is intended to help enforce some specific goals in our samples. Even if not 
specifically mentioned in the format, samples should make best-effort attempts in the following:

* __Easily runnable__ - samples should be as easy for a user to run as possible. Users should be 
  able to copy and paste the code into their own environments and run with as few and transparent 
  modifications as possible.  
  
* __Teach through code__ - samples should teach users how and why specific best practices should
  be implemented and performed when interacting with our services.
  
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

### Project Setup
The README.md should contain instructions for the user to get the samples operable. Ideally, steps
  such as project or resource setup should be links to Cloud Documentation. This is to reduce 
  duplicate instructions and README maintenance in the future. 

### Sample Structure
Each snippet should be be contained in its own file, within a class with a name descriptive of 
  the snippet and a similarly named function. Region tags should start below the package, but should
  include the class and any imports in full. Additional functions can be used if it improves 
  readability of the sample.
  
### Function Parameters
Function parameters should be limited to what is absolutely required for testing. For example, 
 project specific information (such as `projectId`) or a `filePath` for an external file are 
 allowed. A parameter for the type of a file or a specific action is not.
 
Any declared function parameters should include a commented out example of how that parameter could
 be declared. This provides both an example of how to derive the variables and what information they 
 should represent. 
 
### Function Return Type
The function in the snippet should not return any value. Instead, they should print the results of
 actions to the console to be validated later. 
 
### Exception Handling
The snippet should show how to correctly handle Exceptions that occur during the snippet. Top level 
 exceptions can be handled by logging the exception to `System.out.println`. If the exception is 
 something the user may commonly encounter, include a comment explaining how to avoid or handle 
 correctly.

Example:
```java
try {
  // Do something
} catch (IllegalArgumentException e) {
  // IllegalArgumentException's are thrown when an invalid argument has been passed to a function.
  // This error should be logged to that the root cause can be debugged and prevented in the future.
  System.out.println("Error during functionName: \n" + e.toString());
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
} catch (Exception e) {
  System.out.println("Error during functionName: \n" + e.toString());
}
```

### Arrange, Act, Assert
Samples should generally follow the following outline: 
* _Arrange_ - Create and configure the components for the request. Avoid nesting these components,
 as often they use Builders which can hinder readibility. 
* _Act_ - Send the request and receive the response.
* _Assert_ - Verify the call was successful or that the response is correct. This is often done by
 print contents of the response to stdout. 

### Testing
Snippets should have tests that should verify the snippet works and compiles correctly. Creating 
 mocks for these tests are optional. These tests should capture output created by the snippet to 
 verify that it works correctly. See the tests in the cannoncial for an example of how to do this
 correctly. 

### External Resources
Use of environment variables over system properties is strongly preferred for configuration. 

Any additional files required should be stored in `src/test/resources`.

## Additional Best Practices 

The following are some general Java best practices that should be followed in samples to remain
idiomatic. 

### Time
Use the `java.time` package when dealing with units of time in some manner. 

### Logging
Use `java.util.logging` for consistent logging in web applications. 