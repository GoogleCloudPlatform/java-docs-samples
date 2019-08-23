# Using customer-managed encryption keys

This sample demonstrate how to use
[cryptographic encryption keys](https://cloud.google.com/kms/)
for the I/O connectors in an
[Apache Beam](https://beam.apache.org) pipeline.
For more information, see the
[Using customer-managed encryption keys](https://cloud.google.com/dataflow/docs/guides/customer-managed-encryption-keys)
docs page.

## Before you begin

1. Install the [Cloud SDK](https://cloud.google.com/sdk/docs/).

1. [Create a new project](https://console.cloud.google.com/projectcreate)

1. [Enable billing](https://cloud.google.com/billing/docs/how-to/modify-project).

1. [Enable the APIs](https://console.cloud.google.com/flows/enableapi?apiid=dataflow,compute_component,logging,storage_component,storage_api,bigquery,datastore.googleapis.com,cloudresourcemanager.googleapis.com,cloudkms.googleapis.com):
   Dataflow, Compute Engine, Stackdriver Logging, Cloud Storage,
   Cloud Storage JSON, BigQuery, Datastore, Cloud Resource Manager,
   and Cloud KMS API.

1. Setup the Cloud SDK to your GCP project.

   ```sh
   gcloud init
   ```

1. [Create a service account key](https://console.cloud.google.com/apis/credentials/serviceaccountkey) as a JSON file.
   For more information, see
   [Creating and managing service accounts](https://cloud.google.com/iam/docs/creating-managing-service-accounts)

   * From the **Service account** list, select **New service account**.
   * In the **Service account name** field, enter a name.
   * From the **Role** list, select **Project > Owner**.

     > *Note:* The **Role** field authorizes your service account to access resources.
     > You can view and change this field later by using the
     > [GCP Console IAM page](https://console.cloud.google.com/iam-admin/iam).
     > If you are developing a production app, specify more granular permissions than **Project > Owner**.
     > For more information, see
     > [Granting roles to service accounts](https://cloud.google.com/iam/docs/granting-roles-to-service-accounts).

   * Click **Create**. A JSON file that contains your key downloads to your computer.

1. Set your `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your service account key file.

   ```sh
   export GOOGLE_APPLICATION_CREDENTIALS=path/to/your/credentials.json
   ```

1. Set up environment variables for the system setup.

   ```sh
   # Set the project ID and project number and Cloud Storage bucket.
   PROJECT=$(gcloud config get-value project)
   PROJECT_NUMBER=$(gcloud projects list --filter $PROJECT --format "value(PROJECT_NUMBER)")
   BUCKET=your-gcs-bucket

   # Set the KMS keyring and key names.
   KMS_KEYRING=samples-keyring
   KMS_KEY=samples-key
   ```

1. Create a Cloud Storage bucket.

   ```sh
   gsutil mb gs://$BUCKET
   ```

1. [Create a symmetric key ring](https://cloud.google.com/kms/docs/creating-keys).
   For best results, use a [regional location](https://cloud.google.com/kms/docs/locations).
   This example uses a `global` key for simplicity.

   ```sh
   # Create a key ring.
   gcloud kms keyrings create $KMS_KEYRING --location global

   # Create a key.
   gcloud kms keys create $KMS_KEY --location global \
     --keyring $KMS_KEYRING --purpose encryption
   ```

   > *Note:* Although you can destroy the
   > [*key version material*](https://cloud.google.com/kms/docs/destroy-restore),
   > you [cannot delete keys and key rings](https://cloud.google.com/kms/docs/object-hierarchy#lifetime).
   > Key rings and keys do not have billable costs or quota limitations,
   > so their continued existence does not impact costs or production limits.

1. Grant Encrypter/Decrypter permissions to the Dataflow, Compute Engine, and BigQuery accounts.

   ```sh
   # Grant Encrypter/Decrypter permissions to the Dataflow service account.
   gcloud projects add-iam-policy-binding $PROJECT \
     --member serviceAccount:service-$PROJECT_NUMBER@dataflow-service-producer-prod.iam.gserviceaccount.com \
     --role roles/cloudkms.cryptoKeyEncrypterDecrypter

   # Grant Encrypter/Decrypter permissions to the Compute Engine service account.
   gcloud projects add-iam-policy-binding $PROJECT \
     --member serviceAccount:service-$PROJECT_NUMBER@compute-system.iam.gserviceaccount.com \
     --role roles/cloudkms.cryptoKeyEncrypterDecrypter

   # Grant Encrypter/Decrypter permissions to the BigQuery service account.
   gcloud projects add-iam-policy-binding $PROJECT \
     --member serviceAccount:bq-$PROJECT_NUMBER@bigquery-encryption.iam.gserviceaccount.com \
     --role roles/cloudkms.cryptoKeyEncrypterDecrypter
   ```

## Setup

The following instructions help you prepare your development environment.

1. Download and install
   [OpenJDK](https://openjdk.java.net/install/index.html)
   (alternatively, you can use the propietary
   [Oracle JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)).
   Verify that the
   [JAVA_HOME](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/envvars001.html)
   environment variable is set and points to your JDK installation.

   ```sh
   $JAVA_HOME/bin/java --version
   ```

1. Download and install
   [Apache Maven](http://maven.apache.org/download.cgi)
   by following the
   [Maven installation guide](http://maven.apache.org/install.html)
   for your specific operating system.

   ```sh
   mvn --version
   ```

1. Clone the `java-docs-samples` repository.

   ```sh
   git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
   ```

1. Navigate to the sample code directory.

   ```sh
   cd java-docs-samples/dataflow/customer-managed-encryption-keys
   ```

## BigQueryKmsKey example

* [BigQueryKmsKey.java](src/main/java/com/example/dataflow/cmek/BigQueryKmsKey.java)
* [pom.xml](pom.xml)

The following sample will get some data from the
[NASA wildfires public BigQuery dataset](https://console.cloud.google.com/bigquery?p=bigquery-public-data&d=nasa_wildfire&t=past_week&page=table)
using a customer-managed encryption key, and dump that data into the specified `outputBigQueryTable`
using the same customer-managed encryption key.

Make sure you have the following environment variables:

```sh
# Set the project ID, GCS bucket and KMS key.
PROJECT=$(gcloud config get-value project)
BUCKET=your-gcs-bucket

# Set the KMS key ID.
KMS_KEYRING=samples-keyring
KMS_KEY=samples-key
KMS_KEY_ID=$(gcloud kms keys list --location global --keyring $KMS_KEYRING --filter $KMS_KEY --format "value(NAME)")

# Output BigQuery dataset and table name.
DATASET=samples
TABLE=dataflow_kms
```

Create the output BigQuery dataset and table with a schema file.

```sh
# Create the BigQuery dataset.
bq mk --dataset $PROJECT:$DATASET

# Create the BigQuery table.
bq mk --table $PROJECT:$DATASET.$TABLE ./schema.json
```

To run the sample using the Cloud Dataflow runner.

```sh
mvn compile exec:java \
  -Dexec.mainClass=com.example.dataflow.cmek.BigQueryKmsKey \
  -Dexec.args="\
    --outputBigQueryTable=$PROJECT:$DATASET.$TABLE \
    --kmsKey=$KMS_KEY_ID \
    --runner=DataflowRunner \
    --project=$PROJECT \
    --tempLocation=gs://$BUCKET/samples/dataflow/kms/tmp"
```

> *Note:* To run locally you can omit the `--runner` command line argument for it to default to the `DirectRunner`.

You can check your submitted Cloud Dataflow jobs in the [GCP Console Dataflow page](https://console.cloud.google.com/dataflow) or by using `gcloud`.

```sh
gcloud dataflow jobs list
```

Finally, check the contents of the BigQuery table.

```sh
bq query --use_legacy_sql=false "SELECT * FROM `$PROJECT.$DATASET.$TABLE`"
```

## Cleanup

To avoid incurring charges to your GCP account for the resources used:

```sh
# Remove only the files created by this sample.
gsutil -m rm -rf "gs://$BUCKET/samples/dataflow/kms"

# [optional] Remove the Cloud Storage bucket.
gsutil rb gs://$BUCKET

# Remove the BigQuery table.
bq rm -f -t $PROJECT:$DATASET.$TABLE

# [optional] Remove the BigQuery dataset and all its tables.
bq rm -rf -d $PROJECT:$DATASET

# Revoke Encrypter/Decrypter permissions to the Dataflow service account.
gcloud projects remove-iam-policy-binding $PROJECT \
  --member serviceAccount:service-$PROJECT_NUMBER@dataflow-service-producer-prod.iam.gserviceaccount.com \
  --role roles/cloudkms.cryptoKeyEncrypterDecrypter

# Revoke Encrypter/Decrypter permissions to the Compute Engine service account.
gcloud projects remove-iam-policy-binding $PROJECT \
  --member serviceAccount:service-$PROJECT_NUMBER@compute-system.iam.gserviceaccount.com \
  --role roles/cloudkms.cryptoKeyEncrypterDecrypter

# Revoke Encrypter/Decrypter permissions to the BigQuery service account.
gcloud projects remove-iam-policy-binding $PROJECT \
  --member serviceAccount:bq-$PROJECT_NUMBER@bigquery-encryption.iam.gserviceaccount.com \
  --role roles/cloudkms.cryptoKeyEncrypterDecrypter
```
