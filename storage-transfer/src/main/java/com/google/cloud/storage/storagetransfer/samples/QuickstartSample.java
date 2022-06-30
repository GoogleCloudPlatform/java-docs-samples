/*
 * Copyright 2021 Google Inc.
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

// [START storagetransfer_quickstart]
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.CreateTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferProto.RunTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;

public class QuickstartSample {
  /** Quickstart sample using transfer service to transfer from one GCS bucket to another. */
  public static void quickStartSample(
      String projectId, String gcsSourceBucket, String gcsSinkBucket) throws Exception {
    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // The name of the source GCS bucket to transfer objects from
    // String gcsSourceBucket = "your-source-gcs-source-bucket";

    // The name of the  GCS bucket to transfer  objects to
    // String gcsSinkBucket = "your-sink-gcs-bucket";

    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setGcsDataSource(GcsData.newBuilder().setBucketName(gcsSourceBucket))
                    .setGcsDataSink(GcsData.newBuilder().setBucketName(gcsSinkBucket)))
            .setStatus(TransferJob.Status.ENABLED)
            .build();

    TransferJob response =
        storageTransfer.createTransferJob(
            CreateTransferJobRequest.newBuilder().setTransferJob(transferJob).build());

    storageTransfer.runTransferJobAsync(
            RunTransferJobRequest.newBuilder()
            .setProjectId(projectId)
            .setJobName(response.getName())
            .build())
            .get();
    System.out.println(
        "Created and ran transfer job between two GCS buckets with name " + response.getName());
  }
}
// [END storagetransfer_quickstart]
