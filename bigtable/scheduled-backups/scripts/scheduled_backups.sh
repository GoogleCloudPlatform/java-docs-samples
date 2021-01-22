#!/bin/bash
#    Copyright 2020 Google, Inc.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
# PRE-REQUISITES
#    1. Google Cloud SDK must be installed
#    2. The service account must have the following rolas assigned  
#       - Cloud Functions Invoker 
#       - Cloud Scheduler Admin
#    3. The service account must have the following permiissions assigned
#       - appengine.applications.create
#       - serviceusage.services.enable
######

set -xuo pipefail

SCRIPTDIR=$(dirname "$0")
CONFIG="${SCRIPTDIR}/../config/scheduled-backups.properties"
source "$CONFIG"

function print_usage() {
  echo 'Usage: $0 [create-schedule | update-schedule]'
  echo ""
  echo "create-schedule - create a schedule for backup creation using the properties defined in the config/scheduled-backups.properties file"
  echo ""
  echo "update-schedule - update an existing schedule for backup creation using the properties defined in the config/scheduled-backups.properties file"
  echo ""
  echo "deploy-backup-function - deploy the Cloud function that initiates a backup. the function will be invoked by the Cloud Scheduler"
  echo ""
  echo "add-metrics - create and deploy user defined metrics for monitoring and alerting on scheduled backup errors and failures"
  echo ""
}

if [ $# = 0 ]; then
  print_usage
  exit
fi

COMMAND=$1
case $COMMAND in
  --help|-help|-h)
    print_usage
    exit
    ;;

create-schedule) 

  JSON_FMT='{"projectId":"%s", "instanceId":"%s", "tableId":"%s", "clusterId":"%s", "expireHours":%d}'
  SCHEDULE_MESSAGE_BODY="$(printf "$JSON_FMT" "$PROJECT_ID"
  "$BIGTABLE_INSTANCE_ID" "$BIGTABLE_BACKUP_TABLE_NAME"
  "$BIGTABLE_BACKUP_CLUSTER_ID" "$BIGTABLE_BACKUP_EXPIRE_HOURS")"

  gcloud scheduler jobs create pubsub "$SCHEDULE_JOB_NAME" \
    --schedule="$SCHEDULE_JOB_TIMESPEC" \
    --topic="$SCHEDULE_PUBSUB_TOPIC_NAME" \
    --message-body="$SCHEDULE_MESSAGE_BODY" \
    --project "$PROJECT_ID"

  ;;

update-schedule) 

  JSON_FMT='{"projectId":"%s", "instanceId":"%s", "tableId":"%s", "clusterId":"%s", "expireHours":%d}'
  SCHEDULE_MESSAGE_BODY="$(printf "$JSON_FMT" "$PROJECT_ID"
  "$BIGTABLE_INSTANCE_ID" "$BIGTABLE_BACKUP_TABLE_NAME"
  "$BIGTABLE_BACKUP_CLUSTER_ID" "$BIGTABLE_BACKUP_EXPIRE_HOURS")"

  gcloud scheduler jobs update pubsub "$SCHEDULE_JOB_NAME" \
    --schedule="$SCHEDULE_JOB_TIMESPEC" \
    --topic="$SCHEDULE_PUBSUB_TOPIC_NAME" \
    --message-body="$SCHEDULE_MESSAGE_BODY" \
    --project "$PROJECT_ID"
  ;;

deploy-backup-function)  

  gcloud functions deploy "$FUNCTION_CREATE_BACKUP_NAME" \
    --entry-point "$FUNCTION_CREATE_BACKUP_CLASS" \
    --trigger-topic "$SCHEDULE_PUBSUB_TOPIC_NAME" \
    --runtime "$FUNCTION_RUNTIME" \
    --service-account "$SERVICE_ACCOUNT" \
    --project "$PROJECT_ID"
    
  ;;

add-metrics)

  gcloud deployment-manager deployments create "$METRICS_DEPLOYMENT_NAME" \
    --config "$METRICS_CONFIG_FILE" \
    --project "$PROJECT_ID"
  ;;

esac
