/*
 * Copyright 2024 Google LLC
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

package vtwo.iam;

// [START securitycenter_get_iam_policies_v2]

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SourceName;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.GetPolicyOptions;
import com.google.iam.v1.Policy;
import java.io.IOException;

public class GetIamPolicies {
  // Demonstrates how to retrieve IAM policies for a source
  public static Policy getIamPolicySource(String organizationId, String sourceId) {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Start setting up a request to get IAM policy for a source.
      SourceName sourceName = SourceName.ofOrganizationSourceName(organizationId, sourceId);

      GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
          .setResource(sourceName.toString())
          .setOptions(GetPolicyOptions.newBuilder().build())
          .build();

      // Call the API.
      Policy response = client.getIamPolicy(request);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
}
// [END securitycenter_get_iam_policies_v2]

