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

// [START storagetransfer_transfer_from_azure]
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto;
import com.google.storagetransfer.v1.proto.TransferProto.RunTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.AzureBlobStorageData;
import com.google.storagetransfer.v1.proto.TransferTypes.AzureCredentials;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob.Status;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class TransferFromAzure {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Your Google Cloud Project ID
    String projectId = "my-project-id";

    // Your Azure Storage Account name
    String azureStorageAccount = "my-azure-account";

    // The Azure source container to transfer data from
    String azureSourceContainer = "my-source-container";

    // The GCS bucket to transfer data to
    String gcsSinkBucket = "my-sink-bucket";

    transferFromAzureBlobStorage(
        projectId, azureStorageAccount, azureSourceContainer, gcsSinkBucket);
  }

  /**
   * Creates and runs a transfer job to transfer all data from an Azure container to a GCS bucket.
   */
  public static void transferFromAzureBlobStorage(
      String projectId,
      String azureStorageAccount,
      String azureSourceContainer,
      String gcsSinkBucket)
      throws IOException, ExecutionException, InterruptedException {

    // Your Azure SAS token, should be accessed via environment variable
    String azureSasToken = System.getenv("AZURE_SAS_TOKEN");

    TransferSpec transferSpec =
        TransferSpec.newBuilder()
            .setAzureBlobStorageDataSource(
                AzureBlobStorageData.newBuilder()
                    .setAzureCredentials(
                        AzureCredentials.newBuilder().setSasToken(azureSasToken).build())
                    .setContainer(azureSourceContainer)
                    .setStorageAccount(azureStorageAccount))
            .setGcsDataSink(GcsData.newBuilder().setBucketName(gcsSinkBucket).build())
            .build();

    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setStatus(Status.ENABLED)
            .setTransferSpec(transferSpec)
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

      // Run the created job
      storageTransfer
          .runTransferJobAsync(
              RunTransferJobRequest.newBuilder()
                  .setProjectId(projectId)
                  .setJobName(response.getName())
                  .build())
          .get();

      System.out.println(
          "Created and ran a transfer job from "
              + azureSourceContainer
              + " to "
              + gcsSinkBucket
              + " with "
              + "name "
              + response.getName());
    }
  }
}
// [END storagetransfer_transfer_from_azure]
