/* Copyright 2024 Google LLC
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

// [START iam_create_key]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.gson.Gson;
import com.google.iam.admin.v1.CreateServiceAccountKeyRequest;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.IOException;

public class CreateServiceAccountKey {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String projectId = "your-project-id";
    String serviceAccountName = "your-service-account-name";

    ServiceAccountKey key = createKey(projectId, serviceAccountName);
    Gson gson = new Gson();

    // System.out.println("Service account key: " + gson.toJson(key));
  }

  // Creates a key for a service account.
  public static ServiceAccountKey createKey(String projectId, String accountName)
          throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      CreateServiceAccountKeyRequest req = CreateServiceAccountKeyRequest.newBuilder()
              .setName(String.format("projects/%s/serviceAccounts/%s", projectId, email))
              .build();
      ServiceAccountKey createdKey = iamClient.createServiceAccountKey(req);
      System.out.println("Key created successfully");

      return createdKey;
    }
  }
}
// [END iam_create_key]
