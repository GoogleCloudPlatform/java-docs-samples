# Session Management

This demo shows how to use Valkey as an in-memory sessions to store user tokens for quick access in a session management application. By storing user tokens in Valkey, the application can quickly retrieve and validate tokens without having to query the database.

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

### 5. Run the app

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

You can also run the app with sample data by running the following:

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
   Error waiting to create Service: Error waiting for Creating Service: Error code 13, message: Revision 'sessions-app-service-00001-9zj' is not ready and cannot serve traffic. Image 'gcr.io/cloudannot serve traffic. Image 'gcr.io/cloud-memorystore-demos/sessions-app:latest' not found.
   ```

2. Once the infrastructure is created, you'll need to run the `init.sql` script in the Cloud SQL instance to create the necessary tables. You can use the Cloud Shell to do this. Run the following command in the Cloud Shell:

   ```bash
   gcloud sql connect <instance_name> --database=sessions-app-db --user=admin # The admin and database were created in the Terraform script
   ```

   Note: Ensure that the instance name is the same as the one you used in the Terraform script.

   a. When prompted to enable the Cloud SQL Admin app, type `Y` and press `Enter`.
   b. When prompted to enter the password, type the password you set in the Terraform script and press `Enter`.
   c. Once you're connected to the Cloud SQL instance, run the following command to run the `init.sql` script:

   ```sql
   \i init.sql
   ```

3. Finally, redeploy the Cloud Run service using the local source code. Run the following command:

   ```bash
   gcloud run deploy <instance_name> \
    --source=. \
    --region=<region> \
    --update-env-vars=ALLOWED_ORIGINS=example.com \
   ```

   Note: Ensure that the instance name and region are the same as the ones you used in the Terraform script. Also, replace `example.com` with the domain of your frontend application.

Now you should have the application running on Google Cloud.

### Endpoints

- `POST /auth/register` - Registers a new user
- `POST /auth/login` - Logs in a user
- `POST /auth/logout` - Logs out a user
- `POST /auth/verify` - Verifies a user's token
- `GET /api/basket` - Get all items
- `POST /api/basket/add` - Add item with quantity
- `POST /api/basket/remove` - Remove item quantity
- `POST /api/basket/clear` - Clear entire basket
