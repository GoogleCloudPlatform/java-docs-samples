#!/bin/bash

PROJECT_ID=$1
REGION=$2
GCS_BUCKET=$3
PUBSUB_INPUT_TOPIC=$4
PUBSUB_OUTPUT_TOPIC=$5
CBT_INSTANCE=$6
CBT_TABLE=$7

ML_ENDPOINT_ID=$(cat Scripts/ENDPOINT_ID.output)

if { [ -z "$ML_ENDPOINT_ID" ]; }; then
	echo "ML endpoint was not written properly."
	exit 1
fi

echo "PROJECT_ID = $PROJECT_ID"
echo "REGION = $REGION"
echo "GCS_BUCKET = $GCS_BUCKET"
echo "PUBSUB_INPUT_TOPIC = $PUBSUB_INPUT_TOPIC"
echo "PUBSUB_OUTPUT_TOPIC = $PUBSUB_OUTPUT_TOPIC"
echo "CBT_INSTANCE = $CBT_INSTANCE"
echo "CBT_TABLE = $CBT_TABLE"
echo "RANDOM_ID = $RANDOM_ID"
echo "ML_ENDPOINT_ID = $ML_ENDPOINT_ID"

~/homebrew/bin/mvn compile exec:java -f ../pom.xml -Dexec.mainClass=com.example.dataflowpipelines.FraudDetection -Dexec.cleanupDaemonThreads=false \
"-Dexec.args= --runner=DataflowRunner --project=$PROJECT_ID --region=$REGION \
--gcpTempLocation=gs://$GCS_BUCKET/temp --outputTopic=projects/$PROJECT_ID/topics/$PUBSUB_OUTPUT_TOPIC \
--inputTopic=projects/$PROJECT_ID/topics/$PUBSUB_INPUT_TOPIC --projectID=$PROJECT_ID \
--CBTInstanceId=$CBT_INSTANCE --CBTTableId=$CBT_TABLE --MLRegion=$REGION \
--MLEndpoint=$ML_ENDPOINT_ID"
