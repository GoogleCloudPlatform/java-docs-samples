# Stream Cloud Pub/Sub with Cloud DataFlow

Samples showing how to use [Google Cloud Pub/Sub] with [Google Cloud Dataflow].

## Before you begin

1. Install the [Cloud SDK].

1. [Create a new project].

1. [Enable billing].

1. [Enable the APIs](https://console.cloud.google.com/flows/enableapi?apiid=dataflow,compute_component,logging,storage_component,storage_api,pubsub,cloudresourcemanager.googleapis.com,cloudscheduler.googleapis.com): Dataflow, Compute Engine, Stackdriver Logging, Cloud Storage, Cloud Storage JSON, Pub/Sub, Cloud Scheduler, and Cloud Resource Manager.

1. Setup the Cloud SDK to your GCP project.

   ```bash
   gcloud init
   ```

1. [Create a service account key] as a JSON file.
   For more information, see [Creating and managing service accounts].

   * From the **Service account** list, select **New service account**.
   * In the **Service account name** field, enter a name.
   * From the **Role** list, select **Project > Owner**.

     > **Note**: The **Role** field authorizes your service account to access resources.
     > You can view and change this field later by using the [GCP Console IAM page].
     > If you are developing a production app, specify more granular permissions than **Project > Owner**.
     > For more information, see [Granting roles to service accounts].

   * Click **Create**. A JSON file that contains your key downloads to your computer.

1. Set your `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your service account key file.

   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/credentials.json
   ```

1. Create a Cloud Storage bucket. Grant the Default Compute Engine Service Account Object Admin Access to your bucket.

   ```bash
   gsutil mb gs://your-gcs-bucket
   
   BUCKET=gs://your-gcs-bucket
   PROJECT=${gcloud config get-value project}
   PROJECT_NUMBER=$(gcloud projects describe ${PROJECT} --format="value(projectNumber)")
   
   gsutil iam ch \
     serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com:objectAdmin \
     ${BUCKET}
   ```
   
 1. Start a Cloud Scheduler job that publishes one message to a Cloud Pub/Sub topic every minute.
 
    ```bash
    gcloud pubsub topics create cron-topic
    
    gcloud scheduler jobs create pubsub my-job --schedule="* * * * *" \
      --topic=cron-topic --message-body="Hello Pub/Sub!"
    ```

## Setup

The following instructions will help you prepare your development environment.

1. Download and install the [Java Development Kit (JDK)].
   Verify that the [JAVA_HOME] environment variable is set and points to your JDK installation.

1. Download and install [Apache Maven] by following the [Maven installation guide] for your specific operating system.

1. Clone the `java-docs-samples` repository.

    ```bash
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    ```

1. Navigate to the sample code directory.

   ```bash
   cd java-docs-samples/pubsub/streaming-analytics
   ```

## Streaming Analytics

### Google Cloud Pub/Sub to Google Cloud Storage

* [PubSubToGCS.java](src/main/java/com/examples/pubsub/streaming/PubSubToGCS.java)

To run the example and write output files in the desired Cloud Storage location.

```bash
mvn compile exec:java \
  -Dexec.mainClass=com.examples.pubsub.streaming.PubSubToGCS \
  -Dexec.cleanupDaemonThreads=false \
  -Dexec.args="\
    --windowSize=2 \
    --inputTopic=projects/${PROJECT}/topics/cron-topic \
    --output=gs://${BUCKET}/output- \
    --runner=DataflowRunner"
```

After the job has been submitted, you can check its status in the [GCP Console Dataflow page].

## Cleanup

1. Delete the Cloud Schedule job. 
    ```bash
    gcloud scheduler jobs delete my-job
    ```

1. `Ctrl + C` to stop the program in your terminal.

1. Cancel the DataFlow job in [GCP Console Dataflow page]. Stop the pipeline without draining it. 

1. Delete the topic. 
   ```bash
   gcloud pubsub topics delete cron-topic
   ```

1. To avoid incurring charges to your GCP account for the resources used:

    ```bash
    # Delete only the files created by this sample.
    gsutil -m rm -rf "gs://$BUCKET/output*"
    
    # [optional] Remove the Cloud Storage bucket.
    gsutil rb gs://$BUCKET
    ```

[Apache Beam]: https://beam.apache.org/
[Google Cloud Pub/Sub]: https://cloud.google.com/pubsub/docs/
[Google Cloud Dataflow]: https://cloud.google.com/dataflow/docs/

[Cloud SDK]: https://cloud.google.com/sdk/docs/
[Create a new project]: https://console.cloud.google.com/projectcreate
[Enable billing]: https://cloud.google.com/billing/docs/how-to/modify-project
[Create a service account key]: https://console.cloud.google.com/apis/credentials/serviceaccountkey
[Creating and managing service accounts]: https://cloud.google.com/iam/docs/creating-managing-service-accounts
[GCP Console IAM page]: https://console.cloud.google.com/iam-admin/iam
[Granting roles to service accounts]: https://cloud.google.com/iam/docs/granting-roles-to-service-accounts

[Java Development Kit (JDK)]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[JAVA_HOME]: https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/envvars001.html
[Apache Maven]: http://maven.apache.org/download.cgi
[Maven installation guide]: http://maven.apache.org/install.html

[GCP Console create Dataflow job page]: https://console.cloud.google.com/dataflow/createjob
[GCP Console Dataflow page]: https://console.cloud.google.com/dataflow
