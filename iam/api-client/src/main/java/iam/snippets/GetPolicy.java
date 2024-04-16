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

// [START iam_get_policy]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.GetPolicyOptions;
import com.google.iam.v1.Policy;

import java.io.IOException;

public class GetPolicy {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your project ID".
    String projectId = "your-project-id";
    // TODO: Replace with your service account name".
    String serviceAccount = "your-service-account";
    getPolicy(projectId, serviceAccount);
  }

  // Gets a project's policy.
  public static Policy getPolicy(String projectId, String serviceAccount) throws IOException {
    String serviceAccountEmail = serviceAccount + "@" + projectId + ".iam.gserviceaccount.com";
    try(IAMClient iamClient = IAMClient.create()) {
      GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
              .setResource(ServiceAccountName.of(projectId, serviceAccountEmail).toString())
              .setOptions(GetPolicyOptions.newBuilder().build())
              .build();
      Policy policy = iamClient.getIamPolicy(request);
      System.out.println("Policy retrieved: " + policy.toString());
      return policy;
    }
  }
}
// [END iam_get_policy]
