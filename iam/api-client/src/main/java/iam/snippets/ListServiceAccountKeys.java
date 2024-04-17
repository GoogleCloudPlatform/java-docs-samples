/* Copyright 2019 Google LLC
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

// [START iam_list_keys]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ListServiceAccountKeysRequest;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.IOException;
import java.util.List;

public class ListServiceAccountKeys {

  // Lists all keys for a service account.
  public static void listKeys(String projectId, String serviceAccountName) {
    // String projectId = "my-project-id";
    // String serviceAccountName = "my-service-account-name";

    String serviceAccountEmail = serviceAccountName + "@" + projectId + ".iam.gserviceaccount.com";
    try (IAMClient iamClient = IAMClient.create()) {
      ListServiceAccountKeysRequest req = createListKeysReq(projectId, serviceAccountEmail);
      List<ServiceAccountKey> keys = iamClient.listServiceAccountKeys(req)
              .getKeysList();

      keys.forEach(key -> System.out.println("Key: " + key.getName()));
    } catch (IOException ex) {
      System.out.println("Unable to find service account keys");
    }
  }

  private static ListServiceAccountKeysRequest createListKeysReq(String projectId, String email) {
    return ListServiceAccountKeysRequest.newBuilder()
            .setName("projects/" + projectId + "/serviceAccounts/" + email)
            .build();
  }
}
// [END iam_list_keys]
