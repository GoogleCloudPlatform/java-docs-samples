# Java websockets sample for Google Managed VMs
This sample demonstrates how to use websockets on Google Managed VMs
## Setup
Before you can run or deploy the sample, you will need to create a new firewall rule to allow traffic on port 65080. This port will be used for websocket connections. You can do this with the [Google Cloud SDK](https://cloud.google.com/sdk) with the following command:
    $ gcloud compute firewall-rules create default-allow-websockets \
        --allow tcp:65080 \
        --target-tags websocket \
        --description "Allow websocket traffic on port 65080"
