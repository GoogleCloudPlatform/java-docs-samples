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

// [START securitycenter_set_iam_polices_v2]

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SourceName;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.FieldMask;
// [END securitycenter_set_iam_polices_v2]
import java.io.IOException;

public class SetIamPolices {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // The source id corresponding to the finding.
    String sourceId = "{source-id}";

    // Some user email.
    String userEmail = "{user-email}";

    // Identifies the IAM role.
    String roleId = "{role-id}";

    setIamPolicySource(organizationId, sourceId, userEmail, roleId);
  }

  // [START securitycenter_set_iam_polices_v2]
  // Demonstrates how to verify IAM permissions to create findings.
  public static Policy setIamPolicySource(String organizationId, String sourceId, String userEmail,
      String roleId) {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Start setting up a request to set IAM policy for a source.
      SourceName sourceName = SourceName.ofOrganizationSourceName(organizationId, sourceId);

      // userEmail = "someuser@domain.com"
      // Set up IAM Policy for the user userMail to use the role findingsEditor.
      // The user must be a valid Google account.
      Policy oldPolicy = client.getIamPolicy(sourceName.toString());
      Binding bindings =
          Binding.newBuilder()
              .setRole(roleId)
              .addMembers("user:" + userEmail)
              .build();
      Policy policy = oldPolicy.toBuilder().addBindings(bindings).build();

      // Update policy.
      SetIamPolicyRequest request = SetIamPolicyRequest.newBuilder()
          .setResource(sourceName.toString())
          .setPolicy(policy).setUpdateMask(FieldMask.newBuilder().build())
          .build();

      // Call the API.
      Policy response = client.setIamPolicy(request);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
  // [END securitycenter_set_iam_polices_v2]
}
