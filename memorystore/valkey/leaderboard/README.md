# Leaderboard

This demo shows how to use Valkey as an in-memory cache to accelerate data retrieval in a leaderboard application. By storing top scores in Valkey, the application can quickly retrieve the top entries without having to query the database.

## Running the application locally

### 1. Run your database locally. You can download and install PostgresSQL via this [link](https://www.postgresql.org/download/)

### 2. Run your Valkey server locally. You can download and install Valkey via this [link](https://valkey.io/download/)

### 3. Ensure that you have a user created called `postgres`

```bash
createuser -s postgres
```

### 4. Next, create the required database tables

```bash
psql -U postgres -d postgres -f ./app/init.sql
```

### 5. Run the application

```bash
mvn clean spring-boot:run
```

### 6. Navigate to the web url `http://localhost:8080` to view your application

## How to run the application locally (via Docker)

You can use [docker compose](https://docs.docker.com/compose/install/) to run the app locally. Run the following:

```bash
cd app
docker-compose up --build
```

You can also run with sample leaderboard data. Run the following:

```bash
cd sample-data
docker-compose up --build
```

## How to deploy the application to Google Cloud

1. You can use [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) to deploy the infrastructure to Google Cloud. Run the following:

   ```bash
   cd app
   terraform init
   terraform apply
   ```

   It should fail to ceate the Google Cloud Run service, but don't worry, we'll fix that in the next series of steps. The message you get might look like this:

   ```
   Error waiting to create Service: Error waiting for Creating Service: Error code 13, message: Revision 'leaderboard-app-service-00001-9zj' is not ready and cannot serve traffic. Image 'gcr.io/cloudannot serve traffic. Image 'gcr.io/cloud-memorystore-demos/leaderboard-app:latest' not found.
   ```

2. Once the infrastructure is created, you'll need to run the `init.sql` script in the Cloud SQL instance to create the necessary tables. You can use the Cloud Shell to do this. Run the following command in the Cloud Shell:

   ```bash
   gcloud sql connect <instance_name> --database=leaderboard-app-db --user=admin # The admin and database were created in the Terraform script
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

### Endpoints

- `GET /api/leaderboard`: By default, this endpoint returns the top X entries in the leaderboard. Optionally, a parameter `position` can be provided to return the leaderboard starting from that position.
- `POST /api/leaderboard`: This endpoint creates or updates a leaderboard entry with a given username and score.
