# Encrypting fields in Cloud SQL - MySQL with Tink

## Before you begin

1. If you haven't already, set up a Java Development Environment (including google-cloud-sdk and
maven utilities) by following the [java setup guide](https://cloud.google.com/java/docs/setup) and
[create a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. If you haven't already, set up a Python Development Environment by following the [python setup guide](https://cloud.google.com/python/setup) and 
[create a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects#creating_a_project).

1. Create a 2nd Gen Cloud SQL Instance by following these 
[instructions](https://cloud.google.com/sql/docs/mysql/create-instance). Note the connection string,
database user, and database password that you create.

1. Create a database for your application by following these 
[instructions](https://cloud.google.com/sql/docs/mysql/create-manage-databases). Note the database
name.

1. Create a KMS key for your application by following these
[instructions](https://cloud.google.com/kms/docs/creating-keys). Copy the resource name of your
created key.

1. Create a service account with the 'Cloud SQL Client' permissions by following these 
[instructions](https://cloud.google.com/sql/docs/mysql/connect-external-app#4_if_required_by_your_authentication_method_create_a_service_account).
Then, add the 'Cloud KMS CryptoKey Encrypter/Decrypter' permission for the key to your service account 
by following these [instructions](https://cloud.google.com/kms/docs/iam).

## Running Locally

Before running, copy the `example.envrc` file to `.envrc` and replace the values for 
`GOOGLE_APPLICATION_CREDENTIALS`, `DB_USER`, `DB_PASS`, `DB_NAME`, `CLOUD_SQL_CONNECTION_NAME`,
and `CLOUD_KMS_URI` with the values from your project. Then run `source .envrc` or optionally use 
[direnv](https://direnv.net/).

Once the environment variables have been set, run:
```
mvn exec:java -Dexec.mainClass=cloudsql.tink.EncryptAndInsertData
```
and 
```
mvn exec:java -Dexec.mainClass=cloudsql.tink.QueryAndDecryptData
```
