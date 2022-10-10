#!/bin/bash

REGION=$1
UUID=$2

MODEL_NAME=fraud-ml-model-$UUID
EP_NAME=fraud-ml-model-ep-$UUID
DEPLOYED_MODEL_NAME=fraud-ml-deployed-model-$UUID

echo "REGION = $REGION"
echo "MODEL_NAME = $MODEL_NAME"
echo "EP_NAME = $EP_NAME"
echo "DEPLOYED_MODEL_NAME = $DEPLOYED_MODEL_NAME"

MODEL_ID=$(gcloud ai models list \
          --region=$REGION \
          --filter=displayName:$MODEL_NAME \
          --format="value(MODEL_ID.scope())")
ENDPOINT_ID=$(gcloud ai endpoints list \
          --region=$REGION \
          --filter=displayName:$EP_NAME \
          --format="value(ENDPOINT_ID.scope())")
DEPLOYED_MODEL_ID=$(gcloud ai endpoints describe $ENDPOINT_ID --region=$REGION \
          --format="value(deployedModels.id)")

echo "MODEL_ID = $MODEL_ID"
echo "ENDPOINT_ID = $ENDPOINT_ID"
echo "DEPLOYED_MODEL_ID = $DEPLOYED_MODEL_ID"

if ! { [ -z "$ENDPOINT_ID" ] && [ -z "$DEPLOYED_MODEL_ID" ]; }; then
	echo "RAN it"
	yes | gcloud ai endpoints undeploy-model $ENDPOINT_ID \
	        --deployed-model-id=$DEPLOYED_MODEL_ID --region=us-central1
fi

if ! { [ -z "$ENDPOINT_ID" ]; }; then
	yes | gcloud ai endpoints delete $ENDPOINT_ID --region=us-central1
fi

if ! { [ -z "$MODEL_ID" ]; }; then
	yes | gcloud ai models delete $MODEL_ID --region=us-central1
fi

if test -f "./scripts/ENDPOINT_ID-$UUID.output"; then
    rm ./scripts/ENDPOINT_ID-$UUID.output
fi
