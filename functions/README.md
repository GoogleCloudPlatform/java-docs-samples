<img src="https://avatars2.githubusercontent.com/u/2810941?v=3&s=96" alt="Google Cloud Platform logo" title="Google Cloud Platform" align="right" height="96" width="96"/>

# Google Cloud Functions Java Samples

[Cloud Run functions](https://cloud.google.com/functions/docs/concepts/overview) is a lightweight, event-based, asynchronous compute solution that allows you to create small, single-purpose functions that respond to Cloud events without the need to manage a server or a runtime environment.

There are two versions of Cloud Run functions:

* **Cloud Run functions**, formerly known as Cloud Functions (2nd gen), which deploys your function as services on Cloud Run, allowing you to trigger them using Eventarc and Pub/Sub. Cloud Run functions are created using `gcloud functions` or `gcloud run`. Samples for Cloud Run functions can be found in the [`functions/v2`](v2/) folder.
* **Cloud Run functions (1st gen)**, formerly known as Cloud Functions (1st gen), the original version of functions with limited event triggers and configurability. Cloud Run functions (1st gen) are created using `gcloud functions --no-gen2`. Samples for Cloud Run functions (1st generation) can be found in the current `functions/` folder.

## Samples

* [Hello World](helloworld/)
* [Concepts](v2/concepts/)
* [Datastore](v2/datastore/)
* [Firebase](firebase/)
* [Cloud Pub/Sub](v2/pubsub/)
* [HTTP](http/)
* [Logging & Monitoring](logging/)
* [Slack](slack/)
* [OCR tutorial](v2/ocr/)
* [ImageMagick](v2/imagemagick/)

## Running Functions Locally
The [Java Functions Framework](https://github.com/GoogleCloudPlatform/functions-framework-java)
Maven plugin (`com.google.cloud.functions:function-maven-plugin`) allows you to run Java Cloud
Functions code on your local machine for local development and testing purposes. Use the following
Maven command to run a function locally:

```
mvn function:run -Drun.functionTarget=your.package.yourFunction
```
