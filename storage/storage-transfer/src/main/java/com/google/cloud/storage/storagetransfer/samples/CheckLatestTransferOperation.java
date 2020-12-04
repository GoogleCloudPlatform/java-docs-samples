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

import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.StoragetransferScopes;
import com.google.api.services.storagetransfer.v1.model.Operation;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;

public class CheckLatestTransferOperation {

  // Gets the requested transfer job and checks its latest operation
  public static void checkLatestTransferOperation(String projectId, String jobName)
      throws IOException {
    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // The name of the job to check
    // String jobName = "myJob/1234567890";

    // Create Storage Transfer client
    GoogleCredentials credential = GoogleCredentials.getApplicationDefault();
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(StoragetransferScopes.all());
    }
    Storagetransfer storageTransfer = new Storagetransfer.Builder(Utils.getDefaultTransport(),
        Utils.getDefaultJsonFactory(), new HttpCredentialsAdapter(credential))
        .build();

    // Get transfer job and check latest operation
    TransferJob transferJob = storageTransfer.transferJobs().get(jobName, projectId).execute();
    String latestOperationName = transferJob.getLatestOperationName();

    if (latestOperationName != null) {
      Operation latestOperation = storageTransfer.transferOperations().get(latestOperationName)
          .execute();
      System.out.println("The latest operation for transfer job " + jobName + " is:");
      System.out.println(latestOperation.toPrettyString());

    } else {
      System.out.println("Transfer job " + jobName + " does not have an operation scheduled yet,"
          + " try again once the job starts running.");
    }
  }
}
// [END storagetransfer_get_latest_transfer_operation]
