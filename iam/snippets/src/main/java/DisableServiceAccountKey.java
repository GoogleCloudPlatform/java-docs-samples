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

// [START iam_disable_service_account_key]

import com.google.cloud.iam.admin.v1.IAMClient;
import java.io.IOException;


public class DisableServiceAccountKey {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String projectId = "gcloud-project-id";
    String serviceAccountName = "service-account-name";
    String serviceAccountKeyName = "service-account-key-name";

    disableServiceAccountKey(projectId, serviceAccountName, serviceAccountKeyName);
  }

  // Disables a service account key.
  public static void disableServiceAccountKey(String projectId,
                                              String accountName,
                                              String key) throws IOException {
    // Construct the service account email.
    // You can modify the ".iam.gserviceaccount.com" to match the service account name in which
    // you want to disable the key.
    // See, https://cloud.google.com/iam/docs/creating-managing-service-account-keys#disabling
    String email = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);
    String name = String.format("projects/%s/serviceAccounts/%s/keys/%s", projectId, email, key);

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.disableServiceAccountKey(name);

      System.out.println("Disabled service account key: " + name);
    }
  }
}
// [END iam_disable_service_account_key]

