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

// [START iam_service_account_get_policy]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import java.io.IOException;

public class GetServiceAccountPolicy {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your project ID.
    String projectId = "your-project-id";
    // TODO: Replace with your service account name.
    String serviceAccount = "your-service-account";
    getPolicy(projectId, serviceAccount);
  }

  // Gets a service account's IAM policy.
  public static Policy getPolicy(String projectId, String serviceAccount) throws IOException {

    // Construct the service account email.
    // You can modify the ".iam.gserviceaccount.com" to match the name of the service account
    // whose allow policy you want to get.
    String serviceAccountEmail = serviceAccount + "@" + projectId + ".iam.gserviceaccount.com";

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
              .setResource(ServiceAccountName.of(projectId, serviceAccountEmail).toString())
              .build();
      Policy policy = iamClient.getIamPolicy(request);
      System.out.println("Policy retrieved: " + policy.toString());
      return policy;
    }
  }
}
// [END iam_service_account_get_policy]
