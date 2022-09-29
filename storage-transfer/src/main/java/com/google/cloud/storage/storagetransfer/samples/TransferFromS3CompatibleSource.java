/*
 * Copyright 2022 Google Inc.
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

// [START storagetransfer_transfer_from_s3_compatible_source]
import static com.google.storagetransfer.v1.proto.TransferTypes.S3CompatibleMetadata.NetworkProtocol;
import static com.google.storagetransfer.v1.proto.TransferTypes.S3CompatibleMetadata.RequestModel;
import static com.google.storagetransfer.v1.proto.TransferTypes.S3CompatibleMetadata.AuthMethod;

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferTypes;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import java.io.IOException;

public class TransferFromS3CompatibleSource {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Your project id
    String projectId = "my-project-id";

    // The agent pool associated with the S3 compatible data source. If not provided, defaults to
    // the default agent
    String sourceAgentPoolName = "projects/my-project-id/agentPools/transfer_service_default";

    // The S3 compatible bucket name to transfer data from
    String sourceBucketName = "my-bucket-name";

    // The S3 compatible path (object prefix) to transfer data from
    String sourcePath = "path/to/data";

    // The ID of the GCS bucket to transfer data to
    String gcsSinkBucket = "my-sink-bucket";

    // The GCS path (object prefix) to transfer data to
    String gcsPath = "path/to/data";

    // The S3 region of the source bucket
    String region = "us-east-1";

    // The S3 compatible endpoint
    String endpoint = "us-east-1.example.com";

    // The S3 compatible network protocol
    NetworkProtocol protocol = NetworkProtocol.NETWORK_PROTOCOL_HTTPS;

    // The S3 compatible request model
    RequestModel requestModel = RequestModel.REQUEST_MODEL_VIRTUAL_HOSTED_STYLE;

    // The S3 Compatible auth method
    AuthMethod authMethod = AuthMethod.AUTH_METHOD_AWS_SIGNATURE_V4;

    transferFromS3CompatibleSource(
        projectId,
        sourceAgentPoolName,
        sourceBucketName,
        sourcePath,
        region,
        endpoint,
        protocol,
        requestModel,
        authMethod,
        gcsSinkBucket,
        gcsPath);
  }

  public static void transferFromS3CompatibleSource(
      String projectId,
      String sourceAgentPoolName,
      String sourceBucketName,
      String sourcePath,
      String region,
      String endpoint,
      NetworkProtocol protocol,
      RequestModel requestModel,
      AuthMethod authMethod,
      String gcsSinkBucket,
      String gcsPath)
      throws IOException {
    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setSourceAgentPoolName(sourceAgentPoolName)
                    .setAwsS3CompatibleDataSource(
                        TransferTypes.AwsS3CompatibleData.newBuilder()
                            .setRegion(region)
                            .setEndpoint(endpoint)
                            .setBucketName(sourceBucketName)
                            .setPath(sourcePath)
                            .setS3Metadata(
                                TransferTypes.S3CompatibleMetadata.newBuilder()
                                    .setProtocol(protocol)
                                    .setRequestModel(requestModel)
                                    .setAuthMethod(authMethod)
                                    .build())
                            .build())
                    .setGcsDataSink(
                        GcsData.newBuilder().setBucketName(gcsSinkBucket).setPath(gcsPath).build()))
            .setStatus(TransferJob.Status.ENABLED)
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create()) {

      // Create the transfer job
      TransferJob response =
          storageTransfer.createTransferJob(
              TransferProto.CreateTransferJobRequest.newBuilder()
                  .setTransferJob(transferJob)
                  .build());

      System.out.println(
          "Created a transfer job from "
              + sourceBucketName
              + " to "
              + gcsSinkBucket
              + " with "
              + "name "
              + response.getName());
    }
  }
}
// [END storagetransfer_transfer_from_s3_compatible_source]
