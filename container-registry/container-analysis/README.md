# Container Analysis
These samples demonstrate how to interact with the [Container Analysis API](https://cloud-dot-devsite.googleplex.com/container-analysis/api/reference/rest/) through Java.

 
## Getting Started


1.  [Download](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html)  maven to handle the project's dependencies
2. [Register your Google Cloud Platform project with the Container Analysis API]((https://console.cloud.google.com/flows/enableapi?apiid=containeranalysis.googleapis.com))
3. Set your GOOGLE_CLOUD_PROJECT environment variable to your Project ID
4. run `mvn clean verify` to build the project and run the tests

## Samples
- **getDiscoveryInfo**
	- Retrieves the Discovery occurrence created for a specified image
- **getOccurrencesForNote**
	- Retrieves all the occurrences associated with a specified note
- **getOccurrencesForImage**
	- Retrieves all the occurrences associated with a specified image
- **pubSub**
	- Handle incoming occurrences using a pubsub subscription
- **createOccurrenceSubscription**
	- Creates and returns a pubsub subscription object listening to the occurrence topic
