# Transfer Service sample using Java

This app creates two types of transfers using the Transfer Service tool.

<!-- auto-doc-link -->
These samples are used on the following documentation pages:

>
* https://cloud.google.com/storage/transfer/create-client
* https://cloud.google.com/storage/transfer/create-manage-transfer-program

<!-- end-auto-doc-link -->

## Prerequisites

1. Set up a project on Google Cloud Console.
  1. Go to the [Google Cloud Console](https://console.cloud.google.com) and
     create or select your project.  You will need the project ID later.
1. Enable the [Google Storage Transfer API in the Google Cloud
   Console](https://console.cloud.google.com/apis/api/storagetransfer/overview).
1. Within Developers Console, select APIs & auth > Credentials.
  1. Select Add credentials > Service account > JSON key.
  1. Set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` to point to
     your JSON key.
1. Add the Storage Transfer service account as an editor of your project.
  1. To get the email address used for the service account, execute the
     [googleServiceAccounts.get REST
     method](https://cloud.google.com/storage/transfer/reference/rest/v1/googleServiceAccounts/get#try-it).
     There should be a "Try It" section on that page, otherwise execute it in
     the [APIs
     Explorer](https://developers.google.com/apis-explorer/#p/storagetransfer/v1/storagetransfer.googleServiceAccounts.get).
   
     It should output an email address like:
    
     ```
     storage-transfer-1234567890@partnercontent.gserviceaccount.com
     ```
  1. Add this as a member and select the Project -> Editor permission on the
     [Google Cloud Console IAM and Admin
     page](https://console.cloud.google.com/iam-admin/iam/project).
1. Set up gcloud for application default credentials.
  1. `gcloud components update`
  1. `gcloud init`

## Transfer from Amazon S3 to Google Cloud Storage

Creating a one-time transfer from Amazon S3 to Google Cloud Storage.
1. Set up data sink.
  1. Go to the Developers Console and create a bucket under Cloud Storage > Storage Browser.
1. Set up data source.
  1. Go to AWS Management Console and create a bucket.
  1. Under Security Credentials, create an IAM User with access to the bucket.
  1. Create an Access Key for the user. Note the Access Key ID and Secret Access Key.
  1. Set the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables.
1. In AwsRequester.java, fill in the user-provided constants.
1. Compile the package with
   ```
   mvn compile
   ```
1. Run the transfer job with
   ```
   mvn exec:java \
       -Dexec.mainClass="com.google.cloud.storage.storagetransfer.samples.AwsRequester" \
       -DprojectId=your-google-cloud-project-id \
       -DjobDescription="Sample transfer job from S3 to GCS." \
       -DawsSourceBucket=your-s3-bucket-name \
       -DgcsSinkBucket=your-gcs-bucket-name
   ```
  1. Note the job ID in the returned Transfer Job.

## Transfer data from a standard Cloud Storage bucket to a Cloud Storage Nearline bucket

Creating a daily transfer from a standard Cloud Storage bucket to a Cloud Storage Nearline
bucket for files untouched for 30 days.
1. Set up data sink.
  1. Go to the Developers Console and create a bucket under Cloud Storage > Storage Browser.
  1. Select Nearline for Storage Class.
1. Set up data source.
  1. Go to the Developers Console and create a bucket under Cloud Storage > Storage Browser.
1. In NearlineRequester.java, fill in the user-provided constants.
1. Run with `mvn compile` and
   `mvn exec:java -Dexec.mainClass="com.google.cloud.storage.storagetransfer.samples.NearlineRequester"`
  1. Note the job ID in the returned Transfer Job.

## Checking the status of a transfer

1. In RequestChecker.java, fill in the user-provided constants. Use the Job Name you recorded earlier.
1. Run with `mvn compile` and
   `mvn exec:java -Dexec.mainClass="com.google.cloud.storage.storagetransfer.samples.RequestChecker"`

## References

- [Google Storage Transfer API Client
  Library](https://developers.google.com/api-client-library/java/apis/storagetransfer/v1)