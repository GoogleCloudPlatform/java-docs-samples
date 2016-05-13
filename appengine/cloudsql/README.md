# Cloud SQL sample for Google App Engine
This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google App Engine
## Setup
Before you can run or deploy the sample, you will need to create a [Cloud SQL instance](https://cloud.google.com/sql/docs/create-instance)
1. Create a new user and database for the application. The easiest way to do this is via the [Google Developers Console](https://console.developers.google.com/project/_/sql/instances/example-instance2/access-control/users). Alternatively, you can use MySQL tools such as the command line client or workbench.
## Running locally
1. You will need to be running a local instance of MySQL.
1. Update the connection string in ``appengine-web.xml`` with your local MySQL instance values.
<br/>`$ mvn clean appengine:devserver`

## Deploying
1. Update the connection string in ``appengine-web.xml`` with your Cloud SQL instance values.
<br/>`$ mvn clean appengine:update`
