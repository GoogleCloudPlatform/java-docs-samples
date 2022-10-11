terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 4.23.0"
    }
  }
}

provider "google" {
  project     = var.project_id
  region      = var.region
  zone        = var.zone
}

# Create a random string to make each run unique.
resource "random_string" "uuid" {
  length  = 15
  special = false
  upper   = false
}

# Create the Cloud Bigtable instance that will be used.
resource "google_bigtable_instance" "tf-fd-instance" {
  name                = "featurestore-${random_string.uuid.result}"
  deletion_protection = false
  cluster {
    cluster_id   = "featurestore-c1"
    num_nodes    = 1
    storage_type = "SSD"
  }
}

# Create a CBT table and create two column families.
resource "google_bigtable_table" "tf-fd-table" {
  name          = "customer-information-${random_string.uuid.result}"
  instance_name = google_bigtable_instance.tf-fd-instance.name
  column_family {
    family = "metadata"
  }
  column_family {
    family = "history"
  }
}

# Create the pubsub input topic.
resource "google_pubsub_topic" "tf-fd-pubsub-input-topic" {
  name                       = "transaction-stream-${random_string.uuid.result}"
  message_retention_duration = "604800s"
}

# Create the pubsub output topic.
resource "google_pubsub_topic" "tf-fd-pubsub-output-topic" {
  name                       = "fraud-result-stream-${random_string.uuid.result}"
  message_retention_duration = "604800s"
}

# Create the pubsub output topic subscription.
resource "google_pubsub_subscription" "tf-fd-pubsub-output-subscription" {
  name  = "fraud-result-stream-subscription-${random_string.uuid.result}"
  topic = google_pubsub_topic.tf-fd-pubsub-output-topic.name

  message_retention_duration = "604800s"
  ack_deadline_seconds       = 60
}

# Create a GCS bucket that will contain the datasets used.
resource "google_storage_bucket" "tf-fd-bucket" {
  name                        = "fraud-detection-${random_string.uuid.result}"
  location                    = var.region
  force_destroy               = true
  uniform_bucket_level_access = true
}

# Create a temp folder that is used by Dataflow temporary files.
resource "google_storage_bucket_object" "tf-fd-bucket-temp-folder" {
  name    = "temp/"
  content = "."
  bucket  = google_storage_bucket.tf-fd-bucket.name
}

# Create a history-dataset folder that contains all the historical transactions.
resource "google_storage_bucket_object" "tf-fd-bucket-history-dataset-folder" {
  name    = "training_dataset/"
  content = "."
  bucket  = google_storage_bucket.tf-fd-bucket.name
}

# Create a test-dataset folder that contains all the testing datasets.
resource "google_storage_bucket_object" "tf-fd-bucket-test-dataset-folder" {
  name    = "testing_dataset/"
  content = "."
  bucket  = google_storage_bucket.tf-fd-bucket.name
}

# Create a model folder that contains the already-trained ML model.
resource "google_storage_bucket_object" "tf-fd-bucket-model-folder" {
  name    = "ml_model/"
  content = "."
  bucket  = google_storage_bucket.tf-fd-bucket.name
}

# A CSV file that contains fraudulent transactions generated
# by the simulator. This is useful for testing the model.
resource "google_storage_bucket_object" "fraud_transactions" {
  name   = "testing_dataset/fraud_transactions.csv"
  source = "./datasets/testing_data/fraud_transactions.csv"
  bucket = google_storage_bucket.tf-fd-bucket.name
}

# A CSV file that contains legitimate transactions generated
# by the simulator. This is useful for testing the model.
resource "google_storage_bucket_object" "legit_transactions" {
  name   = "testing_dataset/legit_transactions.csv"
  source = "./datasets/testing_data/legit_transactions.csv"
  bucket = google_storage_bucket.tf-fd-bucket.name
}

# A CSV file that contains customers' metadata.
resource "google_storage_bucket_object" "customers" {
  name   = "training_dataset/customers.csv"
  source = "./datasets/training_data/customers.csv"
  bucket = google_storage_bucket.tf-fd-bucket.name
}

# A CSV file that contains the historical transactions
# that were used when training the ML model.
resource "google_storage_bucket_object" "transactions" {
  name   = "training_dataset/transactions.csv"
  source = "./datasets/training_data/transactions.csv"
  bucket = google_storage_bucket.tf-fd-bucket.name
}

# The already trained ML model.
resource "google_storage_bucket_object" "ml_model" {
  name   = "ml_model/model.bst"
  source = "./model/model.bst"
  bucket = google_storage_bucket.tf-fd-bucket.name
}

# Setup the ML model on VertexAI, and create an endpoint
# that will be used by the dataflow pipeline to query
# the ML model.
module "vertexai" {
  source  = "terraform-google-modules/gcloud/google"
  version = "~> 2.0"

  platform = "linux"

  create_cmd_entrypoint  = "${path.module}/scripts/vertexai_build.sh"
  create_cmd_body        = "${var.region} ${random_string.uuid.result} ${google_storage_bucket.tf-fd-bucket.name}"

  destroy_cmd_entrypoint = "${path.module}/scripts/vertexai_destroy.sh"
  destroy_cmd_body       = "${var.region} ${random_string.uuid.result}"
}

# Run the fraud-detection streaming pipeline.
module "dataflow_pipeline" {
  source  = "terraform-google-modules/gcloud/google"
  version = "~> 2.0"

  platform = "linux"

  module_depends_on = [module.vertexai.wait]

  create_cmd_entrypoint = "${path.module}/scripts/run_streaming_pipeline.sh"
  create_cmd_body       = "${var.project_id} ${var.region} ${google_storage_bucket.tf-fd-bucket.name} ${google_pubsub_topic.tf-fd-pubsub-input-topic.name} ${google_pubsub_topic.tf-fd-pubsub-output-topic.name} ${google_bigtable_instance.tf-fd-instance.name} ${google_bigtable_table.tf-fd-table.name} ${random_string.uuid.result}"

  destroy_cmd_entrypoint = "${path.module}/scripts/destroy_streaming_pipeline.sh"
  destroy_cmd_body       = "${var.region} ${random_string.uuid.result}"
}

# Load both metadata and historical data into Cloud Bigtable so that
# the dataflow pipeline can aggregate data properly before querying
# the ML model.

module "load_dataset" {
  source  = "terraform-google-modules/gcloud/google"
  version = "~> 2.0"

  platform = "linux"
  module_depends_on = [module.dataflow_pipeline.wait]

  create_cmd_entrypoint = "${path.module}/scripts/load_dataset.sh"
  create_cmd_body       = "${var.project_id} ${var.region} ${google_bigtable_instance.tf-fd-instance.name} ${google_bigtable_table.tf-fd-table.name} ${google_storage_bucket.tf-fd-bucket.name} ${random_string.uuid.result}"
}
