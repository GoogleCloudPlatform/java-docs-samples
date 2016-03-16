# Cloud Storage sample for Google Managed VMs
This sample demonstrates how to use [Cloud Storage](https://cloud.google.com/storage/) on Google Managed VMs.

## Setup
Before you can run or deploy the sample, you will need to do the following:

1. Enable the Cloud Storage API in the [Google Developers Console](https://console.developers.google.com/project/_/apiui/apiview/storage/overview).
2. Create a Cloud Storage Bucket. You can do this with the [Google Cloud SDK](https://cloud.google.com/sdk) using the following command:

  ```
  $ gsutil mb gs://[your-bucket-name]
  ```
3. Set the default ACL on your bucket to public read in order to serve files directly from Cloud Storage. You can do this with the [Google Cloud SDK](https://cloud.google.com/sdk) using the following command:

  ```
  $ gsutil defacl set public-read gs://[your-bucket-name]
  ```
4. Update the bucket name in `src/main/appengine/app.yaml`. This makes the bucket name an environment variable in deployment. You still need to set the environment variable when running locally, as shown below.

## Running locally
    $ export BUCKET_NAME=[your-bucket-name]
    $ mvn clean jetty:run

## Deploying
    $ mvn clean gcloud:deploy
