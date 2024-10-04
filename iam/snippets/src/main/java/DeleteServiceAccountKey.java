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

// [START iam_delete_key]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DeleteServiceAccountKeyRequest;
import com.google.iam.admin.v1.KeyName;
import java.io.IOException;

public class DeleteServiceAccountKey {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    String projectId = "your-project-id";
    String serviceAccountName = "my-service-account-name";
    String serviceAccountKeyId = "service-account-key-id";

    deleteKey(projectId, serviceAccountName, serviceAccountKeyId);
  }

  // Deletes a service account key.
  public static void deleteKey(String projectId, String accountName,
                               String serviceAccountKeyId) throws IOException {
    //Initialize client that will be used to send requests.
    //This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {

      //Construct the service account email.
      //You can modify the ".iam.gserviceaccount.com" to match the service account name in which
      //you want to delete the key.
      //See, https://cloud.google.com/iam/docs/creating-managing-service-account-keys#deleting

      String accountEmail = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);

      String name = KeyName.of(projectId, accountEmail, serviceAccountKeyId).toString();

      DeleteServiceAccountKeyRequest request = DeleteServiceAccountKeyRequest.newBuilder()
              .setName(name)
              .build();

      // Then you can delete the key
      iamClient.deleteServiceAccountKey(request);

      System.out.println("Deleted key: " + serviceAccountKeyId);
    }
  }
}
// [END iam_delete_key]
