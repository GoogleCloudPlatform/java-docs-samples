# Cloud Data Loss Prevention (DLP) API Samples

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=dlp/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

The [Data Loss Prevention API](https://cloud.google.com/dlp/docs/) provides programmatic access to 
a powerful detection engine for personally identifiable information and other privacy-sensitive data
 in unstructured data streams.

## Setup
- A Google Cloud project with billing enabled
- [Enable](https://console.cloud.google.com/launcher/details/google/dlp.googleapis.com) the DLP API.
- (Local testing) [Create a service account](https://cloud.google.com/docs/authentication/getting-started)
and set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable pointing to the downloaded credentials file.
- (Local testing) Set the `DLP_DEID_WRAPPED_KEY` environment variable to an AES-256 key encrypted ('wrapped') [with a Cloud Key Management Service (KMS) key](https://cloud.google.com/kms/docs/encrypt-decrypt).
- (Local testing) Set the `DLP_DEID_KEY_NAME` environment variable to the path-name of the Cloud KMS key you wrapped `DLP_DEID_WRAPPED_KEY` with.

## Build
This project uses the [Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/usage.html) to build an uber jar.
Run:
```
   mvn clean package
```

## Retrieve InfoTypes
An [InfoType identifier](https://cloud.google.com/dlp/docs/infotypes-categories) represents an element of sensitive data.

[Info types](https://cloud.google.com/dlp/docs/infotypes-reference#global) are updated periodically. Use the API to retrieve the most current 
info types for a given category. eg. HEALTH or GOVERNMENT.
  ```
    java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Metadata -category GOVERNMENT
  ``` 

## Retrieve Categories
[Categories](https://cloud.google.com/dlp/docs/infotypes-categories) provide a way to easily access a group of related InfoTypes.
```
  java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Metadata
``` 

## Run the quickstart

The Quickstart demonstrates using the DLP API to identify an InfoType in a given string.
```
   java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.QuickStart
```

## Inspect data for sensitive elements
Inspect strings, files locally and on Google Cloud Storage and Cloud Datastore kinds with the DLP API.

Note: image scanning is not currently supported on Google Cloud Storage.
For more information, refer to the [API documentation](https://cloud.google.com/dlp/docs). 
Optional flags are explained in [this resource](https://cloud.google.com/dlp/docs/reference/rest/v2beta1/content/inspect#InspectConfig).
```
Commands:
  -s <string>                   Inspect a string using the Data Loss Prevention API.
  -f <filepath>                 Inspects a local text, PNG, or JPEG file using the Data Loss Prevention API.
  -gcs -bucketName <bucketName> -fileName <fileName>  Inspects a text file stored on Google Cloud Storage using the Data Loss
                                          Prevention API.
  -ds -projectId [projectId] -namespace [namespace] - kind <kind> Inspect a Datastore instance using the Data Loss Prevention API.

Options:
  --help               Show help 
  -minLikelihood       [string] [choices: "LIKELIHOOD_UNSPECIFIED", "VERY_UNLIKELY", "UNLIKELY", "POSSIBLE", "LIKELY", "VERY_LIKELY"]
                       [default: "LIKELIHOOD_UNSPECIFIED"]
                       specifies the minimum reporting likelihood threshold.
  -f, --maxFindings    [number] [default: 0]
                       maximum number of results to retrieve
  -q, --includeQuote   [boolean] [default: true] include matching string in results
  -t, --infoTypes      restrict to limited set of infoTypes [ default: []]
                       [ eg. PHONE_NUMBER US_PASSPORT]
```
### Examples
 - Inspect a string:
   ```
   java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -s "My phone number is (123) 456-7890 and my email address is me@somedomain.com"
   ```
 - Inspect a local file (text / image):
   ```
     java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -f resources/test.txt
     java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -f resources/test.png
   ```
- Inspect a file on Google Cloud Storage:
  ```
    java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -gcs -bucketName my-bucket -fileName my-file.txt
  ```
- Inspect a Google Cloud Datastore kind:
  ```
    java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -ds -kind my-kind
  ```

## Automatic redaction of sensitive data
[Automatic redaction](https://cloud.google.com/dlp/docs/classification-redaction) produces an output with sensitive data matches removed.

```
Commands:
  -s <string>                   Source input string
  -r <replacement string>       String to replace detected info types
 Options:
  --help               Show help
  -minLikelihood       choices: "LIKELIHOOD_UNSPECIFIED", "VERY_UNLIKELY", "UNLIKELY", "POSSIBLE", "LIKELY", "VERY_LIKELY"]
                       [default: "LIKELIHOOD_UNSPECIFIED"]
                       specifies the minimum reporting likelihood threshold.
  
  -infoTypes     restrict operation to limited set of info types [ default: []]
                      [ eg. PHONE_NUMBER US_PASSPORT]
```

### Example
- Replace sensitive data in text with `_REDACTED_`:
  ```
    java -cp target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Redact -s "My phone number is (123) 456-7890 and my email address is me@somedomain.com" -r "_REDACTED_"
  ```

## Integration tests
### Setup
- [Create a Google Cloud Storage bucket](https://console.cloud.google.com/storage) and upload [test.txt](src/test/resources/test.txt).
- [Create a Google Cloud Datastore](https://console.cloud.google.com/datastore) kind and add an entity with properties:
  - `property1` : john@doe.com
  - `property2` : 343-343-3435
- Update the Google Cloud Storage path and Datastore kind in [InspectIT.java](src/test/java/com/example/dlp/InspectIT.java).
- Ensure that `GOOGLE_APPLICATION_CREDENTIALS` points to authorized service account credentials file.

## Run
Run all tests:
  ```
     mvn clean verify
  ```
