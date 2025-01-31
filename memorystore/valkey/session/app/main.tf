provider "google" {
  project = "cloud-memorystore-demos"
  region  = "us-central1"
}

data "google_project" "project" {
  project_id = "cloud-memorystore-demos"
}

resource "google_compute_network" "app_network" {
  name = "sessions-app-network"
}

resource "google_compute_firewall" "allow_http" {
  name    = "sessions-app-allow-http-8080"
  network = google_compute_network.app_network.name

  allow {
    protocol = "tcp"
    ports    = ["8080"]
  }

  source_ranges = ["0.0.0.0/0"]

  depends_on = [google_compute_network.app_network]
}

resource "google_cloud_run_v2_service" "api" {
  name     = "sessions-api-service"
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

  instance_id    = "sessions-app-valkey-instance"
  project_id     = data.google_project.project.project_id
  location       = "us-central1"
  node_type      = "SHARED_CORE_NANO"
  shard_count    = 1
  engine_version = "VALKEY_7_2"

  network = google_compute_network.app_network.name

  service_connection_policies = {
    sessions_valkey_scp = {
      subnet_names = [google_compute_network.app_network.name]
    }
  }

  depends_on = [google_compute_network.app_network]
}

resource "google_sql_database_instance" "postgres" {
  name             = "sessions-app-postgres-instance"
  database_version = "POSTGRES_16"
  region           = "us-central1"

  settings {
    edition = "ENTERPRISE"
    tier    = "db-custom-1-3840"

    ip_configuration {
      ipv4_enabled = true

      authorized_networks {
        name  = "sessions-app-access"
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
  name     = "sessions-app-db"
  instance = google_sql_database_instance.postgres.name

  depends_on = [google_sql_database_instance.postgres]
}