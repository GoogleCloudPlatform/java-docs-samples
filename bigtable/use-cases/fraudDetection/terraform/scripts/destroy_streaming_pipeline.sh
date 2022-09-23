#!/bin/bash

REGION=$1
RANDOM_UUID=$2

echo "REGION = $REGION"
echo "RANDOM_UUID = $RANDOM_UUID"

JOB_ID=$(gcloud dataflow jobs list --region $REGION --filter="name=fraud-detection-$RANDOM_UUID" | awk '{print $1}' | grep -wv JOB_ID)

if ! { [ -z "$JOB_ID" ]; }; then
	gcloud dataflow jobs drain --region $REGION $JOB_ID
fi
