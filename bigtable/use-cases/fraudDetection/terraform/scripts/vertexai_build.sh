#!/bin/bash

REGION=$1
UUID=$2
BUCKET_NAME=$3

MODEL_NAME=fraud-ml-model-$UUID
ENDPOINT_NAME=fraud-ml-model-ep-$UUID
DEPLOYED_MODEL_NAME=fraud-ml-deployed-model-$UUID

echo "REGION = $REGION"
echo "MODEL_NAME = $MODEL_NAME"
echo "ENDPOINT_NAME = $ENDPOINT_NAME"
echo "DEPLOYED_MODEL_NAME = $DEPLOYED_MODEL_NAME"
echo "BUCKET_NAME = $BUCKET_NAME"

# If the model id exists, fail.
MODEL_ID=$(gcloud ai models list \
          --region=$REGION \
          --filter=displayName:$MODEL_NAME \
          --format="value(MODEL_ID.scope())")

if ! { [ -z "$MODEL_ID" ]; }; then
  echo "ERROR, the ml model id already exists!"
  exit 1
fi

gcloud ai models upload \
  --region=$REGION \
  --display-name=$MODEL_NAME \
  --container-image-uri=us-docker.pkg.dev/vertex-ai/prediction/xgboost-cpu.0-82:latest \
  --artifact-uri=gs://${BUCKET_NAME}/ml_model/

MODEL_ID=$(gcloud ai models list \
          --region=$REGION \
          --filter=displayName:$MODEL_NAME \
          --format="value(MODEL_ID.scope())")

gcloud ai endpoints create \
  --region=$REGION \
  --display-name=$ENDPOINT_NAME

ENDPOINT_ID=$(gcloud ai endpoints list \
          --region=$REGION \
          --filter=displayName:$ENDPOINT_NAME \
          --format="value(ENDPOINT_ID.scope())")

gcloud ai endpoints deploy-model $ENDPOINT_ID \
  --region=$REGION \
  --model=$MODEL_ID \
  --display-name=$DEPLOYED_MODEL_NAME \
  --traffic-split=0=100

# Export ENDPOINT_ID as it will be used by the dataflow job
echo $ENDPOINT_ID > ./scripts/ENDPOINT_ID-$UUID.output
