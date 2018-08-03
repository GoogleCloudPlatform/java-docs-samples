# Data Format Transformations using Cloud Dataflow and Apache Beam

Utility transforms to transform from one file format to another for a large number of files using
[Apache Beam][apache_beam] running on [Google Cloud Dataflow][dataflow].

The transformations supported by this utility are:
  - CSV to Avro
  - Avro to CSV

## Setup

Setup instructions assume you have an active Google Cloud Project and with an associated billing account.
The following instructions will help you prepare your development environment.

1. Install [Cloud SDK][cloud_sdk].
1. Setup Cloud SDK

       gcloud init


1. Select your Google Cloud Project if not already selected

       gcloud config set project [PROJECT_ID]

1. Clone repository

       git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git

1. Navigate to the sample code directory

       cd dataflow/transforms

## Grant required permissions

The examples are configured for Cloud Dataflow which run on Google Compute Engine.
The Compute Engine default service account requires the permissions
`storage.objects.create`, `storage.objects.get`, and `storage.objects.create` to read and write
 objects in your Google Cloud Storage bucket IAM policy.

Learn more about [Cloud Storage IAM Roles][storage_iam_roles] and [Bucket-level IAM][bucket_iam].

The following steps are optional if:

* If the project you use to run these Dataflow transformations also own the buckets used to read/write objects.
* If the bucket you're reading data from is public, e.g., allUsers are granted `roles/storage.objectViewer` viewer.

1. Get the Compute Engine default service account using the following gcloud command:

       gcloud compute project-info describe

   The default service can be found next to `defaultServiceAccount:` in response after running the command.

1. Grant the `roles/storage.objectViewer` role to the bucket to get and list objects from a Dataflow job:

       gsutil iam ch serviceAccount:[COMPUTE_DEFAULT_SERVICE_ACCOUNT]:objectViewer gs://[BUCKET_NAME]

   * Replace `[COMPUTE_DEFAULT_SERVICE_ACCOUNT]` with the Compute Engine default service account.
   * Replace `[BUCKET_NAME]` with the bucket you use to read your input data.

1. Grant the `roles/storage.objectCreator` role to the bucket to create objects on output from a Dataflow job:

       gsutil iam ch serviceAccount:[COMPUTE_DEFAULT_SERVICE_ACCOUNT]:objectCreator gs://[BUCKET_NAME]

   * Replace `[COMPUTE_DEFAULT_SERVICE_ACCOUNT]` with the Compute Engine default service account.
   * Replace `[BUCKET_NAME]` with the bucket you use to read your input data.

1. If the bucket contains both input and output data, grant the `roles/storage.objectAdmin` role to the default service
   account using the gsutil:

       gsutil iam ch serviceAccount:[COMPUTE_DEFAULT_SERVICE_ACCOUNT]:objectAdmin gs://[BUCKET_NAME]

   * Replace `[COMPUTE_DEFAULT_SERVICE_ACCOUNT]` with the Compute Engine default service account.
   * Replace `[BUCKET_NAME]` with the bucket you use to read and write your input and output data respectively.


## Using transformations

### Avro to CSV transformation

To transform Avro formatted files to Csv use the following command:

```bash
# Example

mvn compile exec:java -Dexec.mainClass=com.example.AvroToCsv \
     -Dexec.args="--avroSchema=gs://bucket/schema.avsc --inputFile=gs://bucket/*.avro --output=gs://bucket/output --runner=Dataflow"
```

Full description of options can be found by using the following command:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.AvroToCsv -Dexec.args="--help=com.example.SampleOptions"
```

### CSV to Avro transformation

To transform CSV formatted files without a header to Avro use the following command:

```bash
# Example

mvn compile exec:java -Dexec.mainClass=com.example.CsvToAvro \
     -Dexec.args="--avroSchema=gs://bucket/schema.avsc --inputFile=gs://bucket/*.csv --output=gs://bucket/output --runner=Dataflow"
```

Full description of options can be found by using the following command:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.CsvToAvro -Dexec.args="--help=com.example.SampleOptions"
```

Existing example does not support headers in a CSV files.

## Run Tests

Tests can be run locally using the DirectRunner.


    mvn verify

[storage_iam_roles]: https://cloud.google.com/storage/docs/access-control/iam-roles
[bucket_iam]: https://cloud.google.com/storage/docs/access-control/iam
[cloud_sdk]: https://cloud.google.com/sdk/docs/
[dataflow]: https://cloud.google.com/dataflow/docs/
[apache_beam]: https://beam.apache.org/
