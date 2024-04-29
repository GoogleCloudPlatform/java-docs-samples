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

package iam.snippets;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.GetServiceAccountKeyRequest;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.IOException;

public class GetServiceAccountKey {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String accountName = "my-service-account-name";
    String projectId = "my-project-id    ";
    String keyName = "my-key-name";

    ServiceAccountKey serviceAccountkey = getServiceAccountKey(projectId, accountName, keyName);

    System.out.println("Service account key: " + serviceAccountkey.getName());
  }

  // Get service account key
  public static ServiceAccountKey getServiceAccountKey(String projectId,
                                                       String account,
                                                       String key)
          throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", account, projectId);
    String name = String.format("projects/%s/serviceAccounts/%s/keys/%s", projectId, email, key);

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.getServiceAccountKey(GetServiceAccountKeyRequest.newBuilder()
              .setName(name)
              .build());
    }
  }
}
