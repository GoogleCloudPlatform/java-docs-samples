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

// [START storagetransfer_create_event_driven_aws_transfer]

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferTypes;

public class CreateEventDrivenAwsTransfer {
  public static void main(String[] args) throws Exception {
    // Your Google Cloud Project ID
    String projectId = "your-project-id";

    // The name of the source AWS bucket to transfer data from
    String s3SourceBucket = "yourS3SourceBucket";

    // The name of the GCS bucket to transfer data to
    String gcsSinkBucket = "your-gcs-bucket";

    // The ARN of the SQS queue to subscribe to
    String sqsQueueArn = "arn:aws:sqs:us-east-1:1234567891011:s3-notification-queue";

    createEventDrivenAwsTransfer(projectId, s3SourceBucket, gcsSinkBucket, sqsQueueArn);
  }

  public static void createEventDrivenAwsTransfer(
      String projectId, String s3SourceBucket, String gcsSinkBucket, String sqsQueueArn)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create()) {

      // The ID used to access your AWS account. Should be accessed via environment variable.
      String awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID");

      // The Secret Key used to access your AWS account. Should be accessed via environment
      // variable.
      String awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

      TransferTypes.TransferJob transferJob =
          TransferTypes.TransferJob.newBuilder()
              .setProjectId(projectId)
              .setTransferSpec(
                  TransferTypes.TransferSpec.newBuilder()
                      .setAwsS3DataSource(
                          TransferTypes.AwsS3Data.newBuilder()
                              .setBucketName(s3SourceBucket)
                              .setAwsAccessKey(
                                  TransferTypes.AwsAccessKey.newBuilder()
                                      .setAccessKeyId(awsAccessKeyId)
                                      .setSecretAccessKey(awsSecretAccessKey))
                              .build())
                      .setGcsDataSink(
                          TransferTypes.GcsData.newBuilder().setBucketName(gcsSinkBucket)))
              .setStatus(TransferTypes.TransferJob.Status.ENABLED)
              .setEventStream(TransferTypes.EventStream.newBuilder().setName(sqsQueueArn).build())
              .build();

      TransferTypes.TransferJob response =
          storageTransfer.createTransferJob(
              TransferProto.CreateTransferJobRequest.newBuilder()
                  .setTransferJob(transferJob)
                  .build());

      System.out.println(
          "Created a transfer job from "
              + s3SourceBucket
              + " to "
              + gcsSinkBucket
              + " subscribed to "
              + sqsQueueArn
              + " with name "
              + response.getName());
    }
  }
}
// [END storagetransfer_create_event_driven_aws_transfer]
