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

// [START storagetransfer_download_to_posix]
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.CreateTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.PosixFilesystem;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import java.io.IOException;

public class DownloadToPosix {

  public static void main(String[] args) throws IOException {

    // TODO(developer): Replace these variables before running the sample.

    // Your project id
    String projectId = "my-project-id";

    // The agent pool associated with the POSIX data sink. Defaults to the default agent if not
    // specified
    String sinkAgentPoolName = "projects/my-project-id/agentPools/transfer_service_default";

    // Your GCS source bucket name
    String gcsSourceBucket = "my-gcs-source-bucket";

    // A directory prefix on the Google Cloud Storage bucket to download from
    String gcsSourcePath = "foo/bar/";

    // The root directory path on the source filesystem
    String rootDirectory = "/path/to/transfer/source";

    downloadToPosix(projectId, sinkAgentPoolName, gcsSourceBucket, gcsSourcePath, rootDirectory);
  }

  public static void downloadToPosix(
      String projectId,
      String sinkAgentPoolName,
      String gcsSourceBucket,
      String gcsSourcePath,
      String rootDirectory)
      throws IOException {
    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setSinkAgentPoolName(sinkAgentPoolName)
                    .setGcsDataSource(
                        GcsData.newBuilder().setBucketName(gcsSourceBucket).setPath(gcsSourcePath))
                    .setPosixDataSink(
                        PosixFilesystem.newBuilder().setRootDirectory(rootDirectory).build())
                    .build())
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
              CreateTransferJobRequest.newBuilder().setTransferJob(transferJob).build());

      System.out.println(
          "Created and ran a transfer job from "
              + gcsSourcePath
              + " to "
              + rootDirectory
              + " with "
              + "name "
              + response.getName());
    }
  }
}
// [END storagetransfer_download_to_posix]
