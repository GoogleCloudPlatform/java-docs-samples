# Cloud Spanner Hibernate Example

This sample application demonstrates using [Hibernate 5.4](https://hibernate.org/orm/releases/5.4/)
with [Google Cloud Spanner](https://cloud.google.com/spanner/).

## Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

## Setup

1.  Follow the set-up instructions in [the documentation](https://cloud.google.com/java/docs/setup).

2.  Enable APIs for your project.
    [Click here](https://console.cloud.google.com/flows/enableapi?apiid=spanner.googleapis.com&showconfirmation=true)
    to visit Cloud Platform Console and enable the Google Cloud Spanner API.

3.  Create a Cloud Spanner instance and database via the Cloud Plaform Console's
    [Cloud Spanner section](http://console.cloud.google.com/spanner).

4.  Enable application default credentials by running the command `gcloud auth application-default login`.

5.  Set the parameters for your Spanner database in the database connection string in
    `src/main/resources/hibernate.cfg.xml`. The database connection string has the following format:
    
    ```
    jdbc:cloudspanner:/projects/{YOUR_PROJECT_ID}/instances/{YOUR_INSTANCE_ID}/databases/{YOUR_DATABASE_ID}
    ```
    
    Replace the placeholders in the string with the information for your Spanner database:
    
    * `YOUR_PROJECT_ID` - The Project ID of your Google Cloud Platform project
    * `YOUR_INSTANCE_ID` - The name of your Spanner instance that you created
    * `YOUR_DATABASE_ID` - The name of your database within the Spanner instance that you created

## Run the Example

Run the following commands on the command line in the project directory:

```
mvn clean compile exec:java
```

These commands will compile the Java files and run the `main` method in
`HibernateSampleApplication.java`

This example opens a transaction and saves a `Person` entity without specifying its `UUID`.
It also saves associated `Payment` records for the person.

This insert query will appear in the application output:

```
Hibernate: insert into PersonsTable (address, name, nickname, id) values (?, ?, ?, ?)
Hibernate: insert into Payment (amount, id) values (?, ?)
Hibernate: insert into WireTransferPayment (wire_id, id) values (?, ?)
Hibernate: insert into Payment (amount, id) values (?, ?)
Hibernate: insert into CreditCardPayment (credit_card_id, id) values (?, ?)
...
```

The saved entities are then retrieved using an HQL query, and the stored person with the generated ID is printed:

```
Hibernate: select person0_.id as id1_0_, person0_.address as address2_0_, person0_.name as name3_0_, person0_.nickname as nickname4_0_ from PersonsTable person0_

There are 1 persons saved in the table:
Person{
 id=688377a3-b884-4beb-886d-6e93317c5542
 name='person'
 nickname='purson'
 address='address'
 payment_amount=800
}
```

You will also be able to view the tables and data that Hibernate created in Spanner through the
[Google Cloud Platform Console](https://console.cloud.google.com/spanner).
