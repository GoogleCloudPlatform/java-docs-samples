/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.storage.storagetransfer.samples;

// [START storagetransfer_create_event_driven_gcs_transfer]

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferTypes;

public class CreateEventDrivenGcsTransfer {
  public static void main(String[] args) throws Exception {
    // Your Google Cloud Project ID
    String projectId = "your-project-id";

    // The name of the GCS AWS bucket to transfer data from
    String gcsSourceBucket = "your-gcs-source-bucket";

    // The name of the GCS bucket to transfer data to
    String gcsSinkBucket = "your-gcs-sink-bucket";

    // The ARN of the PubSub queue to subscribe to
    String sqsQueueArn = "projects/PROJECT_NAME/subscriptions/SUBSCRIPTION_ID";

    createEventDrivenGcsTransfer(projectId, gcsSourceBucket, gcsSinkBucket, sqsQueueArn);
  }

  public static void createEventDrivenGcsTransfer(
      String projectId, String gcsSourceBucket, String gcsSinkBucket, String pubSubId)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create()) {

      TransferTypes.TransferJob transferJob =
          TransferTypes.TransferJob.newBuilder()
              .setProjectId(projectId)
              .setTransferSpec(
                  TransferTypes.TransferSpec.newBuilder()
                      .setGcsDataSource(
                          TransferTypes.GcsData.newBuilder().setBucketName(gcsSourceBucket))
                      .setGcsDataSink(
                          TransferTypes.GcsData.newBuilder().setBucketName(gcsSinkBucket)))
              .setStatus(TransferTypes.TransferJob.Status.ENABLED)
              .setEventStream(TransferTypes.EventStream.newBuilder().setName(pubSubId).build())
              .build();

      TransferTypes.TransferJob response =
          storageTransfer.createTransferJob(
              TransferProto.CreateTransferJobRequest.newBuilder()
                  .setTransferJob(transferJob)
                  .build());

      System.out.println(
          "Created a transfer job between from "
              + gcsSourceBucket
              + " to "
              + gcsSinkBucket
              + " subscribed to "
              + pubSubId
              + " with name "
              + response.getName());
    }
  }
}
// [END storagetransfer_create_event_driven_gcs_transfer]
