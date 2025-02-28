/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

provider "google" {
  project = "cloud-memorystore-demos"
  region  = "us-central1"
}

data "google_project" "project" {
  project_id = "cloud-memorystore-demos"
}

resource "google_compute_network" "app_network" {
  name = "caching-app-network"
}

resource "google_compute_firewall" "allow_http" {
  name    = "caching-app-allow-http-8080"
  network = google_compute_network.app_network.name

  allow {
    protocol = "tcp"
    ports    = ["8080"]
  }

  source_ranges = ["0.0.0.0/0"]

  depends_on = [google_compute_network.app_network]
}

resource "google_cloud_run_v2_service" "app" {
  name     = "caching-app-service"
  location = "us-central1"

  template {
    containers {
      image = "replace" # Will be set at a later time

      env {
        name  = "VALKEY_HOST"
        value = module.valkey.discovery_endpoints[0]["address"]
      }

      env {
        name  = "VALKEY_PORT"
        value = module.valkey.discovery_endpoints[0]["port"]
      }

      env {
        name  = "DB_URL"
        value = "jdbc:postgresql://${google_sql_database_instance.postgres.public_ip_address}/${google_sql_database.postgres_db.name}"
      }

      env {
        name  = "DB_USERNAME"
        value = google_sql_user.postgres_user.name
      }

      env {
        name  = "DB_PASSWORD"
        value = google_sql_user.postgres_user.password
      }

      ports {
        container_port = 8080
      }
    }

    vpc_access {
      network_interfaces {
        network = google_compute_network.app_network.name
        subnetwork = google_compute_network.app_network.name
        tags = []
      }
    }
  }

  depends_on = [
    google_compute_network.app_network,
    module.valkey,
    google_sql_database_instance.postgres
  ]
}

module "valkey" {
  source         = "terraform-google-modules/memorystore/google//modules/valkey"
  version        = "12.0"

  instance_id    = "caching-app-valkey-instance"
  project_id     = data.google_project.project.project_id
  location       = "us-central1"
  node_type      = "SHARED_CORE_NANO"
  shard_count    = 1
  engine_version = "VALKEY_7_2"

  network = google_compute_network.app_network.name

  service_connection_policies = {
    caching_valkey_scp = {
      subnet_names = [google_compute_network.app_network.name]
    }
  }

  depends_on = [google_compute_network.app_network]
}

resource "google_sql_database_instance" "postgres" {
  name             = "caching-app-postgres-instance"
  database_version = "POSTGRES_16"
  region           = "us-central1"

  settings {
    edition = "ENTERPRISE"
    tier    = "db-custom-1-3840"

    ip_configuration {
      ipv4_enabled = true

      authorized_networks {
        name  = "caching-app-access"
        value = "0.0.0.0/0"
      }
    }
  }

  depends_on = [google_compute_network.app_network]
}

resource "google_sql_user" "postgres_user" {
  name     = "admin"
  instance = google_sql_database_instance.postgres.name
  password = "password123" # Set this to the password you want to use for the user

  depends_on = [google_sql_database_instance.postgres]
}

resource "google_sql_database" "postgres_db" {
  name     = "caching-app-db"
  instance = google_sql_database_instance.postgres.name

  depends_on = [google_sql_database_instance.postgres]
}