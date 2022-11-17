/*
 * Copyright 2022 Google LLC
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

package kms;

// [START kms_check_state_import_job]
import com.google.cloud.kms.v1.ImportJob;
import com.google.cloud.kms.v1.ImportJobName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import java.io.IOException;

public class CheckStateImportJob {

  public void checkStateImportJob() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String importJobId = "my-import-job";
    checkStateImportJob(projectId, locationId, keyRingId, importJobId);
  }

  // Check the state of an import job in Cloud KMS.
  public void checkStateImportJob(String projectId, String locationId,
                                  String keyRingId, String importJobId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client =
             KeyManagementServiceClient.create()) {
      // Build the parent name from the project, location, and key ring.
      ImportJobName importJobName =
          ImportJobName.of(projectId, locationId, keyRingId, importJobId);

      // Retrieve the state of an existing import job.
      ImportJob importJob = client.getImportJob(importJobName);
      System.out.printf("Current state of import job %s: %s%n",
                        importJob.getName(), importJob.getState());
    }
  }
}
// [END kms_check_state_import_job]
