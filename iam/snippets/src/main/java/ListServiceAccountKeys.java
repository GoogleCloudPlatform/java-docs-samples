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

// [START iam_list_keys]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ListServiceAccountKeysRequest;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.IOException;
import java.util.List;

public class ListServiceAccountKeys {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String projectId = "your-project-id";
    String serviceAccountName = "your-service-account-name";

    List<ServiceAccountKey> keys = listKeys(projectId, serviceAccountName);
    keys.forEach(key -> System.out.println("Key: " + key.getName()));
  }

  // Lists all keys for a service account.
  public static List<ServiceAccountKey> listKeys(String projectId, String accountName)
          throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    String email = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);
    try (IAMClient iamClient = IAMClient.create()) {
      ListServiceAccountKeysRequest req = ListServiceAccountKeysRequest.newBuilder()
              .setName(String.format("projects/%s/serviceAccounts/%s", projectId, email))
              .build();

      return iamClient.listServiceAccountKeys(req).getKeysList();
    }
  }
}
// [END iam_list_keys]
