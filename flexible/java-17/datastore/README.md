# Datastore sample for App Engine Flex

[Documentation](https://cloud.google.com/appengine/docs/flexible/using-firestore-in-datastore-mode?tab=java)

## Setup

Before you can run or deploy the sample, you will need to do the following:

1. Enable the Cloud Storage API in the [Google Developers
   Console](https://console.developers.google.com/project/_/apiui/apiview/storage/overview).
1. Create a [new database](https://cloud.google.com/datastore/docs/store-query-data#create_a_database).
   By default, your Database ID will be `(default)`. In this example, we will be using the "(default)" database.

   Note: Choosing between Native Mode and Datastore Mode? Check [this document](https://cloud.google.com/datastore/docs/firestore-or-datastore)

1. Ensure you assign the appropriate permissions/roles for your Application default service account to perfrom database creation and read & write

## Deploying

    ```sh
    mvn clean package appengine:deploy
    ```
