# Getting Started with Google Cloud Authentication

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=auth/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

See the [documentation][auth-docs] for more information about authenticating for Google Cloud APIs.

[auth-docs]: https://cloud.google.com/docs/authentication/production

## Quickstart

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests

You can then run a given `ClassName` via:

	mvn exec:java -Dexec.mainClass=com.example.storage.ClassName \
	    -DpropertyName=propertyValue \
		-Dexec.args="any arguments to the app"

### Listing buckets with default credentials

    mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.AuthExample

### Listing buckets with credentials in json file

    mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.AuthExample
        -Dexec.args="explicit [path-to-credentials-json]"

### Listing buckets while running on a Google Compute Engine instance

    mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.AuthExample
        -Dexec.args="compute"

### Analyze text sentiment using LanguageService API with API key authentication

Create an API key via the Google Cloud console: https://developers.google.com/workspace/guides/create-credentials#api-key

Once you have an API key you can run the command below replacing [api key] with your API key

    mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.ApiKeyAuthExample
        -Dexec.args=[api key]

## Downscoping with Credential Access Boundaries

The same configuration above applies. 

To run the samples for [Downscoping with Credential Access Boundaries](https://cloud.google.com/iam/docs/downscoping-short-lived-credentials)
you must provide both a bucket name and object name under the TODO(developer): in the main method of `DownscopingExample`. 

You can then run `DownscopingExample` via:

	mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.DownscopingExample

## Tests
Run all tests:
```
   mvn clean verify
```
