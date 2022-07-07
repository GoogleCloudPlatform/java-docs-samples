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

// [START storagetransfer_transfer_posix_to_posix]
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.PosixFilesystem;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import java.io.IOException;

public class TransferBetweenPosix {
  public static void transferBetweenPosix(
      String projectId,
      String sourceAgentPoolName,
      String sinkAgentPoolName,
      String rootDirectory,
      String destinationDirectory,
      String bucketName)
      throws IOException {
    // Your project id
    // String projectId = "my-project-id";

    // The agent pool associated with the POSIX data source. If not provided, defaults to the
    // default agent
    // String sourceAgentPoolName = "projects/my-project/agentPools/transfer_service_default";

    // The agent pool associated with the POSIX data sink. If not provided, defaults to the default
    // agent
    // String sinkAgentPoolName = "projects/my-project/agentPools/transfer_service_default";

    // The root directory path on the source filesystem
    // String rootDirectory = "/directory/to/transfer/source"

    // The root directory path on the sink filesystem
    // String destinationDirectory = "/directory/to/transfer/sink"

    // The ID of the GCS bucket for intermediate storage
    // String bucketName = "my-intermediate-bucket";

    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setSinkAgentPoolName(sinkAgentPoolName)
                    .setSourceAgentPoolName(sourceAgentPoolName)
                    .setPosixDataSource(
                        PosixFilesystem.newBuilder().setRootDirectory(rootDirectory).build())
                    .setPosixDataSink(
                        PosixFilesystem.newBuilder().setRootDirectory(destinationDirectory).build())
                    .setGcsIntermediateDataLocation(
                        GcsData.newBuilder().setBucketName(bucketName).build())
                    .build())
            .setStatus(TransferJob.Status.ENABLED)
            .build();

    // Create a Transfer Service client
    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    // Create the transfer job
    TransferJob response =
        storageTransfer.createTransferJob(
            TransferProto.CreateTransferJobRequest.newBuilder()
                .setTransferJob(transferJob)
                .build());

    System.out.println(
        "Created and ran a transfer job from "
            + rootDirectory
            + " to "
            + destinationDirectory
            + " with name "
            + response.getName());
  }
}
// [END storagetransfer_transfer_posix_to_posix]
