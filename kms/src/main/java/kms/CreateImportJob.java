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

// [START kms_create_import_job]
import com.google.cloud.kms.v1.ImportJob;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyRingName;
import com.google.cloud.kms.v1.ProtectionLevel;
import com.google.cloud.kms.v1.ImportJob.ImportMethod;

import java.io.IOException;

public class CreateImportJob {

  public void createImportJob() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    String keyRingId = "my-key-ring";
    String id = "my-import-job";
    createImportJob(projectId, locationId, keyRingId, id);
  }

  // Create a new import job.
  public void createImportJob(String projectId, String locationId,
      String keyRingId, String id) throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the parent name from the project, location, and key ring.
      KeyRingName keyRingName = KeyRingName.of(projectId, locationId, keyRingId);

      // Build the import job to create, with parameters.
      ImportJob importJob = ImportJob
          .newBuilder()
          // See allowed values and their descriptions at
          // https://cloud.google.com/kms/docs/algorithms#protection_levels
          .setProtectionLevel(ProtectionLevel.HSM)
          // See allowed values and their descriptions at
          // https://cloud.google.com/kms/docs/key-wrapping#import_methods
          .setImportMethod(ImportMethod.RSA_OAEP_3072_SHA1_AES_256)
          .build();

      // Create the import job.
      ImportJob createdImportJob = client.createImportJob(keyRingName, id, importJob);
      System.out.printf("Created import job %s%n", createdImportJob.getName());
    }
  }
}
// [END kms_create_import_job]
