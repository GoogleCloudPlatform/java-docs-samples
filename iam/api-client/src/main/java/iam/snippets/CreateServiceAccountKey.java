/* Copyright 2022 Google LLC
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

// [START iam_create_key]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.CreateServiceAccountKeyRequest;
import com.google.iam.admin.v1.ServiceAccountKey;
import java.io.IOException;

public class CreateServiceAccountKey {

  // Creates a key for a service account.
  public static String createKey(String projectId, String accountName) {
    // String projectId = "project-id";
    // String accountName = "my-service-account-name";
    String email = accountName + "@" + projectId + ".iam.gserviceaccount.com";
    try (IAMClient iamClient = IAMClient.create()) {
      CreateServiceAccountKeyRequest req = createAccountKeyReq(projectId, email);
      ServiceAccountKey createdKey = iamClient.createServiceAccountKey(req);
      System.out.println("Key created successfully");

      String keyName = createdKey.getName();
      return keyName.substring(keyName.lastIndexOf("/") + 1).trim();
    } catch (IOException ex) {
      System.out.println("Failed to create key");
      return null;
    }
  }

  private static CreateServiceAccountKeyRequest createAccountKeyReq(String projId, String email) {
    return CreateServiceAccountKeyRequest.newBuilder()
            .setName("projects/" + projId + "/serviceAccounts/" + email)
            .build();
  }
}
// [END iam_create_key]
