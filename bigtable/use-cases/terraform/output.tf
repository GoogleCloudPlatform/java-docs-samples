output "random_id" {
  description = "The random uuid used in this Terraform run"
  value       = random_string.random_id.result
}

output "project_id" {
  description = "The ID of the project in which to provision resources."
  value       = var.project_id
}

output "region" {
  description = "The region of the project in which to provision resources."
  value       = var.region
}

output "zone" {
  description = "The zone within the region in which to provision resources."
  value       = var.zone
}

output "gcs_bucket" {
  description = "The GCS bucket used in this Terraform run."
  value       = google_storage_bucket.tf-fd-bucket.name
}

output "cbt_instance" {
  description = "The Cloud Bigtable instance used in this Terraform run."
  value       = google_bigtable_instance.tf-fd-instance.name
}

output "cbt_table" {
  description = "The Cloud Bigtable table used in this Terraform run."
  value       = google_bigtable_table.tf-fd-table.name
}

output "pubsub_input_topic" {
  description = "The pub/sub input topic used in this Terraform run."
  value       = google_pubsub_topic.tf-fd-pubsub-input-topic.name
}

output "pubsub_output_topic" {
  description = "The pub/sub output topic used in this Terraform run."
  value       = google_pubsub_topic.tf-fd-pubsub-output-topic.name
}

output "pubsub_output_subscription" {
  description = "The pub/sub output subscription used in this Terraform run."
  value       = google_pubsub_subscription.tf-fd-pubsub-output-subscription.name
}


