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

// [START iam_create_service_account]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.CreateServiceAccountRequest;
import com.google.iam.admin.v1.ProjectName;
import com.google.iam.admin.v1.ServiceAccount;
import java.io.IOException;

public class CreateServiceAccount {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    String projectId = "your-project-id";
    String serviceAccountName = "my-service-account-name";

    createServiceAccount(projectId, serviceAccountName);
  }

  // Creates a service account.
  public static ServiceAccount createServiceAccount(String projectId, String serviceAccountName)
          throws IOException {
    ServiceAccount serviceAccount = ServiceAccount
            .newBuilder()
            .setDisplayName("your-display-name")
            .build();
    CreateServiceAccountRequest request = CreateServiceAccountRequest.newBuilder()
            .setName(ProjectName.of(projectId).toString())
            .setAccountId(serviceAccountName)
            .setServiceAccount(serviceAccount)
            .build();
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      serviceAccount = iamClient.createServiceAccount(request);
      System.out.println("Created service account: " + serviceAccount.getEmail());
    }
    return serviceAccount;
  }
}
// [END iam_create_service_account]
