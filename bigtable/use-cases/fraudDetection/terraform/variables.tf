variable "project_id" {
  description = "The ID of the project in which to provision resources."
  type        = string
}

variable "region" {
  description = "The region of the project in which to provision resources."
  type        = string
  default = "us-central1"
}

variable "zone" {
  description = "The zone within the region in which to provision resources."
  type        = string
  default = "us-central1-c"
}
