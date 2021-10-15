/*
 * Copyright 2020 Google Inc.
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

// [START storagetransfer_get_latest_transfer_operation]

import com.google.longrunning.Operation;
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.GetTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferOperation;
import java.io.IOException;

public class CheckLatestTransferOperation {

  // Gets the requested transfer job and checks its latest operation
  public static void checkLatestTransferOperation(String projectId, String jobName)
      throws IOException {
    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // The name of the job to check
    // String jobName = "myJob/1234567890";

    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    // Get transfer job and check latest operation
    TransferJob transferJob =
        storageTransfer.getTransferJob(
            GetTransferJobRequest.newBuilder().setJobName(jobName).setProjectId(projectId).build());
    String latestOperationName = transferJob.getLatestOperationName();

    if (!latestOperationName.isEmpty()) {
      Operation operation = storageTransfer.getOperationsClient().getOperation(latestOperationName);
      TransferOperation latestOperation =
          TransferOperation.parseFrom(operation.getMetadata().getValue());

      System.out.println("The latest operation for transfer job " + jobName + " is:");
      System.out.println(latestOperation.toString());

    } else {
      System.out.println(
          "Transfer job "
              + jobName
              + " hasn't run yet,"
              + " try again once the job starts running.");
    }
  }
}
// [END storagetransfer_get_latest_transfer_operation]
