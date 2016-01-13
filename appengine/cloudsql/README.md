# Cloud SQL sample for Google Managed VMs
This sample demonstrates how to use [Cloud SQL](https://cloud.google.com/sql/) on Google Managed VMs
## Setup
Before you can run or deploy the sample, you will need to do the following:
1. Create a Cloud SQL instance. You can do this from the [Google Developers Console](https://console.developers.google.com) or via the [Cloud SDK](https://cloud.google.com/sdk). To create it via the SDK use the following command:
    $ gcloud sql instances create [your-instance-name] \
        --assign-ip \
        --authorized-networks 0.0.0.0/0 \
        --tier D0
1. Create a new user and database for the application. The easiest way to do this is via the [Google Developers Console](https://console.developers.google.com/project/_/sql/instances/example-instance2/access-control/users). Alternatively, you can use MySQL tools such as the command line client or workbench.
## Running locally
1. Update the connection string in ``appengine-web.xml`` with your local MySQL instance values.
    $ mvn clean jetty:run
## Deploying
1. Update the connection string in ``appengine-web.xml`` with your Cloud SQL instance values.
    $ mvn clean gcloud:deploy
