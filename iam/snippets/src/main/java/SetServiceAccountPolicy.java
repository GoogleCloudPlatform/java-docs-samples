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

// [START iam_service_account_set_policy]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SetServiceAccountPolicy {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your project ID.
    String projectId = "your-project-id";
    // TODO: Replace with your service account name.
    String serviceAccount = "your-service-account";
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount).
    Policy policy = Policy.newBuilder().build();

    setServiceAccountPolicy(policy, projectId, serviceAccount);
  }

  // Sets a service account's policy.
  public static Policy setServiceAccountPolicy(Policy policy, String projectId,
                                               String serviceAccount) throws IOException {

    // Construct the service account email.
    // You can modify the ".iam.gserviceaccount.com" to match the name of the service account
    // whose allow policy you want to set.
    String accountEmail = String.format("%s@%s.iam.gserviceaccount.com", serviceAccount, projectId);

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      List<String> paths = Arrays.asList("bindings", "etag");
      SetIamPolicyRequest request = SetIamPolicyRequest.newBuilder()
              .setResource(ServiceAccountName.of(projectId, accountEmail).toString())
              .setPolicy(policy)
              // A FieldMask specifying which fields of the policy to modify. Only
              // the fields in the mask will be modified. If no mask is provided, the
              // following default mask is used:
              // `paths: "bindings, etag"`
              .setUpdateMask(FieldMask.newBuilder().addAllPaths(paths).build())
              .build();

      return iamClient.setIamPolicy(request);
    }
  }
}
// [END iam_service_account_set_policy]
