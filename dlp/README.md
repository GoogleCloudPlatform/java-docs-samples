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
   mvn clean package -DskipTests
```

## Retrieve InfoTypes
An [InfoType identifier](https://cloud.google.com/dlp/docs/infotypes-categories) represents an element of sensitive data.

[InfoTypes](https://cloud.google.com/dlp/docs/infotypes-reference#global) are updated periodically. Use the API to retrieve the most current InfoTypes.
  ```
    java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Metadata
  ``` 

## Run the quickstart

The Quickstart demonstrates using the DLP API to identify an InfoType in a given string.
```
   java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.QuickStart
```

## Inspect data for sensitive elements
Inspect strings, files locally and on Google Cloud Storage, Cloud Datastore, and BigQuery with the DLP API.

Note: image scanning is not currently supported on Google Cloud Storage.
For more information, refer to the [API documentation](https://cloud.google.com/dlp/docs). 
Optional flags are explained in [this resource](https://cloud.google.com/dlp/docs/reference/rest/v2beta1/content/inspect#InspectConfig).
```
usage: com.example.dlp.Inspect
 -bq,--Google BigQuery         inspect BigQuery table
 -bucketName <arg>
 -customDictionaries <arg>
 -customRegexes <arg>
 -datasetId <arg>
 -ds,--Google Datastore        inspect Datastore kind
 -f,--file path <arg>          inspect input file path
 -fileName <arg>
 -gcs,--Google Cloud Storage   inspect GCS file
 -includeQuote <arg>
 -infoTypes <arg>
 -kind <arg>
 -maxFindings <arg>
 -minLikelihood <arg>
 -namespace <arg>
 -projectId <arg>
 -s,--string <arg>             inspect string
 -subscriptionId <arg>
 -tableId <arg>
 -topicId <arg>
```
### Examples
 - Inspect a string:
   ```
   java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -s "My phone number is (123) 456-7890 and my email address is me@somedomain.com" -infoTypes PHONE_NUMBER EMAIL_ADDRESS
   java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -s "My phone number is (123) 456-7890 and my email address is me@somedomain.com" -customDictionaries me@somedomain.com -customRegexes "\(\d{3}\) \d{3}-\d{4}"
   ```
 - Inspect a local file (text / image):
   ```
     java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -f src/test/resources/test.txt -infoTypes PHONE_NUMBER EMAIL_ADDRESS
     java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -f src/test/resources/test.png -infoTypes PHONE_NUMBER EMAIL_ADDRESS
   ```
- Inspect a file on Google Cloud Storage:
  ```
    java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -gcs -bucketName my-bucket -fileName my-file.txt -infoTypes PHONE_NUMBER EMAIL_ADDRESS
  ```
- Inspect a Google Cloud Datastore kind:
  ```
    java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Inspect -ds -kind my-kind -infoTypes PHONE_NUMBER EMAIL_ADDRESS
  ```

## Automatic redaction of sensitive data from images
[Automatic redaction](https://cloud.google.com/dlp/docs/redacting-sensitive-data-images) produces an output image with sensitive data matches removed.

```
Commands:
  -f <string>                   Source image file
  -o <string>                   Destination image file
 Options:
  --help               Show help
  -minLikelihood       choices: "LIKELIHOOD_UNSPECIFIED", "VERY_UNLIKELY", "UNLIKELY", "POSSIBLE", "LIKELY", "VERY_LIKELY"]
                       [default: "LIKELIHOOD_UNSPECIFIED"]
                       specifies the minimum reporting likelihood threshold.
  
  -infoTypes      set of infoTypes to search for [eg. PHONE_NUMBER US_PASSPORT]
```

### Example
- Redact phone numbers and email addresses from `test.png`:
  ```
    java -cp dlp/target/dlp-samples-1.0-jar-with-dependencies.jar com.example.dlp.Redact -f src/test/resources/test.png -o test-redacted.png -infoTypes PHONE_NUMBER EMAIL_ADDRESS
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
