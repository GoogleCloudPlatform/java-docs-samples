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

// [START iam_list_service_accounts]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccount;
import java.io.IOException;

public class ListServiceAccounts {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String projectId = "your-project-id";

    IAMClient.ListServiceAccountsPagedResponse response = listServiceAccounts(projectId);

    for (ServiceAccount account : response.iterateAll()) {
      System.out.println("Name: " + account.getName());
      System.out.println("Display name: " + account.getDisplayName());
      System.out.println("Email: " + account.getEmail() + "\n");
    }
  }

  // Lists all service accounts for the current project.
  public static IAMClient.ListServiceAccountsPagedResponse listServiceAccounts(String projectId)
          throws IOException {
    // String projectId = "my-project-id"

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.listServiceAccounts("projects/" + projectId);
    }
  }
}
// [END iam_list_service_accounts]
