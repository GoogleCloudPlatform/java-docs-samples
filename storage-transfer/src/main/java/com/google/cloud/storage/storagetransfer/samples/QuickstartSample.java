package com.google.cloud.storage.storagetransfer.samples;

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.CreateTransferJobRequest;
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

    TransferJob transferJob =
        TransferJob.newBuilder()
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setGcsDataSource(GcsData.newBuilder().setBucketName(gcsSourceBucket))
                    .setGcsDataSink(GcsData.newBuilder().setBucketName(gcsSinkBucket)))
            .setStatus(TransferJob.Status.ENABLED)
            .build();

    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    TransferJob response =
        storageTransfer.createTransferJob(
            CreateTransferJobRequest.newBuilder().setTransferJob(transferJob).build());
    System.out.println("Created transfer job between two GCS buckets:");
    System.out.println(response.toString());
  }
}
