# Cloud Run Image Processing Sample

This sample service applies [Cloud Storage](https://cloud.google.com/storage/docs)-triggered image processing with [Cloud Vision API](https://cloud.google.com/vision/docs) analysis and ImageMagick transformation.

Use it with the [Image Processing with Cloud Run tutorial](http://cloud.google.com/run/docs/tutorials/image-processing).

For more details on how to work with this sample read the [Google Cloud Run Java Samples README](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/run).

[![Run in Google Cloud][run_img]][run_link]

## Dependencies

* **Spring Boot**: Web server framework.
* **Image Magick**: for image processing.
* **@google-cloud/storage**: Google Cloud Storage client library.
* **@google-cloud/vision**: Cloud Vision API client library.
* **Jib**: Container build tool.

## Environment Variables

Cloud Run services can be [configured with Environment Variables](https://cloud.google.com/run/docs/configuring/environment-variables).
Required variables for this sample include:

* `BLURRED_BUCKET_NAME`: The Cloud Run service will write blurred images to this Cloud Storage bucket.

[run_img]: https://storage.googleapis.com/cloudrun/button.svg
[run_link]: https://deploy.cloud.run/?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&dir=run/image-processing
