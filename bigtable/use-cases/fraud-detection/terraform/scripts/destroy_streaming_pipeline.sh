#!/bin/bash

REGION=$1

echo "REGION = $REGION"

JOB_ID=$(gcloud dataflow jobs list --region $REGION --filter="name=fd-streaming-pipeline" | awk '{print $1}' | grep -wv JOB_ID)

if ! { [ -z "$JOB_ID" ]; }; then
	gcloud dataflow jobs drain --region $REGION $JOB_ID
fi
