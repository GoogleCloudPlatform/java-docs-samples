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

// [START storagetransfer_get_transfer_job]
import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.StoragetransferScopes;
import com.google.api.services.storagetransfer.v1.model.ListOperationsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;

public class CheckTransferJob {
  // Performs a list operation to check the status of a job
  public static void checkTransferJob(String projectId, String jobName) throws IOException {
    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // The name of the job to check
    // String jobName = "my-job-name";

    // Create a Transfer Service client
    GoogleCredentials credential = GoogleCredentials.getApplicationDefault();
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(StoragetransferScopes.all());
    }
    Storagetransfer storageTransfer = new Storagetransfer.Builder(Utils.getDefaultTransport(),
        Utils.getDefaultJsonFactory(), new HttpCredentialsAdapter(credential))
        .build();

    // Filter for operations with jobName
    ListOperationsResponse response = storageTransfer
        .transferOperations()
        .list("transferOperations", projectId)
        .setFilter("{\"project_id\": \"" + projectId + "\", \"job_names\": [\"" + jobName + "\"] }")
        .execute();

    System.out.println("List operation returned response:");
    System.out.println(response.toPrettyString());
  }
}
// [END storagetransfer_get_transfer_job]
