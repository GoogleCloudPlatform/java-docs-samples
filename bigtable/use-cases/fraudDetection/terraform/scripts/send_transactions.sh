#!/bin/bash

# This script send a transaction every 5 seconds to the input Cloud Pub/Sub
# topic. It alternates between sending a legitimate and a fraudulent transactions.

# Setting the necessery variables.
NUMBER_OF_LINES=5000
PUBSUB_TOPIC=$(terraform -chdir=../ output pubsub_input_topic | tr -d '"')
FRAUD_TRANSACTIONS_FILE="../datasets/testing_data/fraud_transactions.csv"
LEGIT_TRANSACTIONS_FILE="../datasets/testing_data/legit_transactions.csv"

for i in $(eval echo "{1..$NUMBER_OF_LINES}")
do
  # Send a fraudulent transaction
  MESSAGE=$(sed "${i}q;d" $FRAUD_TRANSACTIONS_FILE)
  echo ${MESSAGE}
  gcloud pubsub topics publish ${PUBSUB_TOPIC} --message="${MESSAGE}"
  sleep 5

  # Send a legit transaction
  MESSAGE=$(sed "${i}q;d" $LEGIT_TRANSACTIONS_FILE)
  echo ${MESSAGE}
  gcloud pubsub topics publish ${PUBSUB_TOPIC} --message="${MESSAGE}"
  sleep 5
done

