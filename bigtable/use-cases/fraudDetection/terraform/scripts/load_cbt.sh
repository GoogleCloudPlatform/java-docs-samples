#!/bin/bash

# Load demographics and historical transactions data from GCS into CBT.

PROJECT_ID=$1
REGION=$2
CBT_INSTANCE=$3
CBT_TABLE=$4
GCS_BUCKET=$5
RANDOM_UUID=$6

echo "PROJECT_ID=$PROJECT_ID"
echo "REGION=$REGION"
echo "CBT_INSTANCE=$CBT_INSTANCE"
echo "CBT_TABLE=$CBT_TABLE"
echo "GCS_BUCKET=$GCS_BUCKET"

mvn -f ../pom.xml compile exec:java -Dexec.mainClass=com.example.dataflowpipelines.LoadCBT -Dexec.cleanupDaemonThreads=false \
"-Dexec.args= --runner=DataflowRunner --project=$PROJECT_ID --projectID=$PROJECT_ID --region=$REGION \
--gcpTempLocation=gs://$GCS_BUCKET/temp --CBTInstanceId=$CBT_INSTANCE --CBTTableId=$CBT_TABLE \
--demographicsInputFile=gs://$GCS_BUCKET/training_dataset/customers.csv \
--historyInputFile=gs://$GCS_BUCKET/training_dataset/transactions.csv \
--randomUUID=$RANDOM_UUID"
