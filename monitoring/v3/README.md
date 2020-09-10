# Cloud Monitoring Sample

Simple command-line program to demonstrate connecting to the Google
Monitoring API to retrieve and modify Alerts data.

## Prerequisites to run locally:

* [Maven 3](https://maven.apache.org)
* [GCloud CLI](https://cloud.google.com/sdk/gcloud/)
* Create a Cloud project

# Set Up Your Local Dev Environment

Create local credentials by running the following command and following the oauth2 flow:

```bash
gcloud auth application-default login
```

To run:

```bash
mvn clean install  
./manage_alerts_sample.sh "<command> <args>"
```

```
usage: list [-p <PROJECT_ID>]
Lists alert policies.
 -p,--projectid <PROJECT_ID>   Your Google project id.

usage: [backup|restore] [-j <JSON_PATH>] [-p <PROJECT_ID>]
Backs up or restores alert policies.
 -j,--jsonPath <JSON_PATH>     Path to json file where alert polices are
                               saved and restored.
 -p,--projectid <PROJECT_ID>   Your Google project id.

usage: replace-channels -a <ALERT_ID> [-c <CHANNEL_ID>] [-p <PROJECT_ID>]
Replaces alert policy notification channels.
 -a,--alert-id <ALERT_ID>       The id of the alert policy whose channels
                                will be replaced.
 -c,--channel-id <CHANNEL_ID>   A channel id.  Repeat this option to set
                                multiple channel ids.
 -p,--projectid <PROJECT_ID>    Your Google project id.

usage: [enable|disable] [-d <FILTER>] [-p <PROJECT_ID>]
Enables/disables alert policies.
 -d,--filter <FILTER>          See
                               https://cloud.google.com/monitoring/api/v3/
                               filters.
 -p,--projectid <PROJECT_ID>   Your Google project id.
 ```
# Running on GCE, GAE, or other environments

On Google App Engine, the credentials should be found automatically.

On Google Compute Engine, the credentials should be found automatically, but require that
you create the instance with the correct scopes.

    gcloud compute instances create --scopes="https://www.googleapis.com/auth/cloud-platform,https://www.googleapis.com/auth/compute,https://www.googleapis.com/auth/compute.readonly" test-instance

If you did not create the instance with the right scopes, you can still upload a JSON service
account and set `GOOGLE_APPLICATION_CREDENTIALS` as described below.

# Using a Service Account

In non-Google Cloud environments, GCE instances created without the correct scopes, or local
workstations if the `gcloud beta auth application-default login` command fails, use a Service
Account by doing the following:

* Go to API Manager -> Credentials
* Click 'New Credentials', and create a Service Account
* Download the JSON for this service account, and set the `GOOGLE_APPLICATION_CREDENTIALS`
 environment variable to point to the file containing the JSON credentials.


    export GOOGLE_APPLICATION_CREDENTIALS=~/Downloads/<project-id>-0123456789abcdef.json
