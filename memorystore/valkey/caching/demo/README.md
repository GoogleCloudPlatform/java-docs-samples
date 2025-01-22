# Caching Demo Application

This demo shows how to use Valkey as an in-memory cache to accelerate data retrieval in a Spring Boot application. By storing hot data in Valkey, you can reduce the number of queries to your PostgreSQL database, improving performance and scalability.

## Running locally

## Run locally using Docker

You can use [docker compose](https://docs.docker.com/compose/install/) to run the application locally. Run the following:

```bash
cd api
docker-compose up --build
```

You can also run with sample data. Run the following:

```bash
cd sample-data
docker-compose up --build
```

You can also run with a web application. Run the following:

```bash
cd web
docker-compose up --build
```

Once the application is running, you can access it at [http://localhost:8080](http://localhost:8080) for local development, or the URL provided by Cloud Run for production.

## How to deploy the application to Google Cloud (Solo API)

1. You can use [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) to deploy the infrastructure to Google Cloud. Run the following:

   ```bash
   cd api
   terraform init
   terraform apply
   ```

   It should fail to ceate the Google Cloud Run service, but don't worry, we'll fix that in the next series of steps. The message you get might look like this:

   ```bash
   Error waiting to create Service: Error waiting for Creating Service: Error code 13, message: Revision 'caching-app-service-00001-9zj' is not ready and cannot serve traffic. Image 'gcr.io/cloudannot serve traffic. Image 'gcr.io/cloud-memorystore-demos/caching-app:latest' not found.
   ```

2. Once the infrastructure is created, you'll need to run the `init.sql` script in the Cloud SQL instance to create the necessary tables. You can use the Cloud Shell to do this. Run the following command in the Cloud Shell:

   ```bash
   gcloud sql connect <instance_name> --database=caching-app-db --user=admin # The admin and database were created in the Terraform script
   ```

   Note: Ensure that the instance name is the same as the one you used in the Terraform script.

   a. When prompted to enable the Cloud SQL Admin API, type `Y` and press `Enter`.
   b. When prompted to enter the password, type the password you set in the Terraform script and press `Enter`.
   c. Once you're connected to the Cloud SQL instance, run the following command to run the `init.sql` script:

   ```sql
   \i init.sql
   ```

3. Finally, redeploy the Cloud Run service using the local source code. Run the following command:

   ```bash
   gcloud run deploy <instance_name> \
    --source=. \
    --region=<region>
   ```

   Note: Ensure that the instance name and region are the same as the ones you used in the Terraform script.

Now you should have the application running on Google Cloud.

## How to deploy the application to Google Cloud (Web App + API)

1. Push your Docker images for the Web App and API to Google Container Registry. Run the following:

   ```bash
   cd web
   docker build -t gcr.io/<project_id>/caching-app-web .
   docker push gcr.io/<project_id>/caching-app-web

   cd ../api
   docker build -t gcr.io/<project_id>/caching-app-api .
   docker push gcr.io/<project_id>/caching-app-api
   ```

2. You can use [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) to deploy the infrastructure to Google Cloud. Run the following:

   ```bash
   cd web
   terraform init
   terraform apply
   ```

3. Once the infrastructure is created, you'll need to run the `init.sql` script in the Cloud SQL instance to create the necessary tables. You can use the Cloud Shell to do this. Run the following command in the Cloud Shell:

   ```bash
   gcloud sql connect <instance_name> --database=caching-app-db --user=admin # The admin and database were created in the Terraform script
   ```

   Note: Ensure that the instance name is the same as the one you used in the Terraform script.

   a. When prompted to enable the Cloud SQL Admin API, type `Y` and press `Enter`.
   b. When prompted to enter the password, type the password you set in the Terraform script and press `Enter`.
   c. Once you're connected to the Cloud SQL instance, run the following command to run the `init.sql` script:

   ```sql
   \i init.sql
   ```

Now you should have the application running on Google Cloud.

### Endpoints

- `GET /item/{id}`: Get an item by ID
- `POST /item/create`: Create a new item
- `DELETE /item/delete/{id}`: Delete an item by ID
