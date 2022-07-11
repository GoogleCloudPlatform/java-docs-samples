#!/bin/bash

REGION=$1
AI_NAME=$2
BUCKET_NAME=$3

MODEL_NAME=tf-fd-ml-model
ENDPOINT_NAME=tf-fd-ml-model-ep
DEPLOYED_MODEL_NAME=tf-fd-ml-deployed-model

echo "REGION = $REGION"
echo "MODEL_NAME = $MODEL_NAME"
echo "ENDPOINT_NAME = $ENDPOINT_NAME"
echo "DEPLOYED_MODEL_NAME = $DEPLOYED_MODEL_NAME"
echo "BUCKET_NAME = $BUCKET_NAME"

# If the model id exists, fail.
MODEL_ID=$(gcloud ai models list   --region=$REGION   --filter=display_name=$MODEL_NAME 2>/dev/null | grep "$MODEL_NAME" | awk '{ print $1 }')
if ! { [ -z "$MODEL_ID" ]; }; then
  echo "ERROR, the ml model id already exists!"
  exit 1
fi

gcloud ai models upload \
  --region=$REGION \
  --display-name=$MODEL_NAME \
  --container-image-uri=us-docker.pkg.dev/vertex-ai/prediction/xgboost-cpu.0-82:latest \
  --artifact-uri=gs://${BUCKET_NAME}/ml_model/
MODEL_ID=$(gcloud ai models list   --region=$REGION   --filter=display_name=$MODEL_NAME 2>/dev/null | grep "$MODEL_NAME" | awk '{ print $1 }')


gcloud ai endpoints create \
  --region=$REGION \
  --display-name=$ENDPOINT_NAME
ENDPOINT_ID=$(gcloud ai endpoints list --region=$REGION --filter=display_name=$ENDPOINT_NAME 2>/dev/null | grep "$ENDPOINT_NAME" | awk '{ print $1 }')

gcloud ai endpoints deploy-model $ENDPOINT_ID \
  --region=$REGION \
  --model=$MODEL_ID \
  --display-name=$DEPLOYED_MODEL_NAME \
  --traffic-split=0=100
DEPLOYED_MODEL_ID=$(gcloud ai endpoints describe $ENDPOINT_ID --region=$REGION 2>/dev/null | grep "id:" | awk -F  "'" '{print $2}')

# Export ENDPOINT_ID as it will be used by the dataflow job
echo $ENDPOINT_ID > ./Scripts/ENDPOINT_ID.output
