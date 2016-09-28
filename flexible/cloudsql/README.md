# Cloud SQL sample for Google Managed VMs
This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google Managed VMs.

## Setup
Before you can run or deploy the sample, you will need to do the following:

1. Create a [Second Generation Cloud SQL](https://cloud.google.com/sql/docs/create-instance) instance. You can do this from the [Cloud Console](https://console.developers.google.com) or via the [Cloud SDK](https://cloud.google.com/sdk). To create it via the SDK use the following command:

        $ gcloud sql instances create YOUR_INSTANCE_NAME \
            --activation-policy=ALWAYS \
            --tier=db-n1-standard-1

1. Set the root password on your Cloud SQL instance:

        $ gcloud sql instances set-root-password YOUR_INSTANCE_NAME --password YOUR_INSTANCE_ROOT_PASSWORD

1. Use the MySQL command line tools (or a management tool of your choice) to create a [new user](https://cloud.google.com/sql/docs/create-user) and [database](https://cloud.google.com/sql/docs/create-database) for your application:

    $ mysql -h [IP Address of database] -u root -p
    mysql> create database YOUR_DATABASE;
    mysql> create user 'YOUR_USER'@'%' identified by 'PASSWORD';
    mysql> grant all on YOUR_DATABASE.* to 'YOUR_USER'@'%';

1. Set the connection string environment variable in src/main/appengine/app.yaml

## Running locally
    Export local variables
    $ export SQL_URL="jdbc:mysql://google/YOUR-DB-NAME?cloudSqlInstance=YOUR-INSTANCE-NAME&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=USERNAME&password=PASSWORD"
    $ mvn clean jetty:run

## Deploying
    $ mvn clean gcloud:deploy
