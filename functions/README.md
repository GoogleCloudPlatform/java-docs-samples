<img src="https://avatars2.githubusercontent.com/u/2810941?v=3&s=96" alt="Google Cloud Platform logo" title="Google Cloud Platform" align="right" height="96" width="96"/>

# Google Cloud Functions Java Samples

[Cloud Functions][functions_docs] is a lightweight, event-based, asynchronous
compute solution that allows you to create small, single-purpose functions that
respond to Cloud events without the need to manage a server or a runtime
environment.

[functions_docs]: https://cloud.google.com/functions/docs/

## Samples

* [Hello World](helloworld/)
* [Concepts](concepts/)
* [Firebase](firebase/)
* [Cloud Pub/Sub](pubsub/)
* [HTTP](http/)
* [Logging & Monitoring](logging/)
* [Slack](slack/)
* [OCR tutorial](ocr/)
* [ImageMagick](imagemagick/)
* [CI/CD setup](ci_cd/)

## Running Functions Locally
The [Java Functions Framework](https://github.com/GoogleCloudPlatform/functions-framework-java)
Maven plugin (`com.google.cloud.functions:function-maven-plugin`) allows you to run Java Cloud
Functions code on your local machine for local development and testing purposes. Use the following
Maven command to run a function locally:

```
mvn function:run -Drun.functionTarget=your.package.yourFunction
```
