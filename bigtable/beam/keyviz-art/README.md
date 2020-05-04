# Google Cloud Bigtable Key Visualizer art

This code allows you to create various works of art in Cloud Bigtable's [key visualizer](https://cloud.google.com/bigtable/docs/keyvis-overview).

![Example image with Mona Lisa](mona_lisa_example.png)


## Setup

1. [Create a Bigtable instance](https://cloud.google.com/bigtable/docs/creating-instance)

1. Set your variables
```
BIGTABLE_PROJECT=YOUR-PROJECT-ID OR $GOOGLE_CLOUD_PROJECT
INSTANCE_ID=YOUR-INSTANCE-ID
TABLE_ID=YOUR-TABLE-ID
```
Your Bigtable can be the same or different than the project you want to run your
Dataflow job in.

1. Create a table

```
echo project = $BIGTABLE_PROJECT > ~/.cbtrc
echo instance = $INSTANCE_ID >> ~/.cbtrc

cbt createtable $TABLE_ID
cbt createfamily $TABLE_ID cf
```

1. Make sure your Dataflow API is enabled

```
gcloud services enable dataflow.googleapis.com
```

## Load data
Load 40GB of data with 5MB rows:
```
mvn compile exec:java -Dexec.mainClass=keyviz.LoadData \
"-Dexec.args=--bigtableProjectId=$BIGTABLE_PROJECT \
--bigtableInstanceId=$INSTANCE_ID --runner=dataflow \
--bigtableTableId=$TABLE_ID --project=$GOOGLE_CLOUD_PROJECT"
```

Load 50GB of data with 1MB rows:
```
mvn compile exec:java -Dexec.mainClass=keyviz.LoadData \
"-Dexec.args=--bigtableProjectId=$BIGTABLE_PROJECT \
--bigtableInstanceId=$INSTANCE_ID --runner=dataflow \
--bigtableTableId=$TABLE_ID --project=$GOOGLE_CLOUD_PROJECT \
--gigabytesWritten=50 \
--megabytesPerRow=1"
```


## Read data and generate image

Generate Mona Lisa with 40GB total and 5MB rows:
```
mvn compile exec:java -Dexec.mainClass=keyviz.ReadData \
"-Dexec.args=--bigtableProjectId=$BIGTABLE_PROJECT \
--bigtableInstanceId=$INSTANCE_ID --runner=dataflow \
--bigtableTableId=$TABLE_ID --project=$GOOGLE_CLOUD_PROJECT"
```

Generate American Gothic  with 50GB total and 1MB rows:
```
mvn compile exec:java -Dexec.mainClass=keyviz.ReadData \
"-Dexec.args=--bigtableProjectId=$BIGTABLE_PROJECT \
--bigtableInstanceId=$INSTANCE_ID --runner=dataflow \
--bigtableTableId=$TABLE_ID --project=$GOOGLE_CLOUD_PROJECT \
--gigabytesWritten=50 \
--megabytesPerRow=1 \
--filePath=gs://keyviz-art/american_gothic_4h.txt"
```

### Generate other images:
There is a [bucket with existing images](https://console.cloud.google.com/storage/browser/keyviz-art) you can use. 
Or you can [create your own with this tool](https://codepen.io/billyjacobson/pen/OJVxVzO), and then upload them to your own GCS bucket.

Filenames are made from `gs://keyviz-art/[painting]_[hours]h.txt`
example: `gs://keyviz-art/american_gothic_4h.txt`

painting options:
* american_gothic
* mona_lisa
* pearl_earring
* persistence_of_memory
* starry_night
* sunday_afternoon
* the_scream

hour options: 
* 1
* 4
* 8
* 12
* 24
* 48
* 72
* 96
* 120
* 144


# Cancel all your jobs

This is a quick command to cancel all your dataflow jobs if you start a few. 

```
gcloud dataflow jobs list --status=active --region=YOUR-REGION |  tail -n +2 | sed 's/ .*//' | xargs gcloud dataflow jobs cancel
```
