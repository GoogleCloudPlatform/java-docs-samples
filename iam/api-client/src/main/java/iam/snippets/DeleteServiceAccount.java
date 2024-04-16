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

// [START iam_delete_service_account]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DeleteServiceAccountRequest;
import com.google.iam.admin.v1.ServiceAccountName;

import java.io.IOException;

public class DeleteServiceAccount {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    String projectId = "your-project-id";
    String serviceAccountName = "my-service-account-name";

    deleteServiceAccount(projectId, serviceAccountName);
  }

  // Deletes a service account.
  public static void deleteServiceAccount(String projectId, String serviceAccountName) throws IOException {
    try(IAMClient client = IAMClient.create()) {
      DeleteServiceAccountRequest request = DeleteServiceAccountRequest.newBuilder()
              .setName(ServiceAccountName.of(projectId, serviceAccountName).toString() + "@" + projectId + ".iam.gserviceaccount.com")
              .build();
      client.deleteServiceAccount(request);

      System.out.println("Deleted service account: " + serviceAccountName);
    }
  }
}
// [END iam_delete_service_account]
