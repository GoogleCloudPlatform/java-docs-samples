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

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccount;
import java.io.IOException;

public class GetServiceAccount {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String name = "your-service-account-name";
    String projectId = "your-project-id";

    ServiceAccount serviceAccount = getServiceAccount(projectId, name);

    System.out.println("Service account name: " + serviceAccount.getDisplayName());
    System.out.println("Service account email: " + serviceAccount.getEmail());
    System.out.println("Service account description: " + serviceAccount.getDescription());
  }

  // Get service account
  public static ServiceAccount getServiceAccount(String projectId, String accountName)
          throws IOException {
    String email = String.format("%s@%s.iam.gserviceaccount.com", accountName, projectId);
    String accountFullName = String.format("projects/%s/serviceAccounts/%s", projectId, email);

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.getServiceAccount(accountFullName);
    }
  }
}
