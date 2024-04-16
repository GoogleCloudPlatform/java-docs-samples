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

// [START iam_set_policy]
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ProjectName;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.Arrays;

public class SetPolicy {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your project ID".
    String projectId = "your-project-id";
    // TODO: Replace with your service account name".
    String serviceAccount = "your-service-account";
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount)".
    Policy policy = Policy.newBuilder().build();

    setPolicy(policy, projectId, serviceAccount);
  }

  // Sets a project's policy.
  public static void setPolicy(Policy policy, String projectId, String serviceAccount) throws IOException {
    // policy = service.Projects.GetIAmPolicy(new GetIamPolicyRequest(), your-project-id).Execute();
    // projectId = "my-project-id"
    String serviceAccountEmail = serviceAccount + "@" + projectId + ".iam.gserviceaccount.com";
    try(IAMClient iamClient = IAMClient.create()) {
      SetIamPolicyRequest request = SetIamPolicyRequest.newBuilder()
              .setResource(ServiceAccountName.of(projectId, serviceAccountEmail).toString())
              .setPolicy(policy)
              //A FieldMask specifying which fields of the policy to modify. Only
              //  the fields in the mask will be modified. If no mask is provided, the
              //  following default mask is used:
              //  `paths: "bindings, etag"`
              .setUpdateMask(FieldMask.newBuilder().addAllPaths(Arrays.asList("bindings", "etag")).build())
              .build();
      Policy response = iamClient.setIamPolicy(request);
      System.out.println("Policy set: " + response.toString());
    }
  }
}
// [END iam_set_policy]
