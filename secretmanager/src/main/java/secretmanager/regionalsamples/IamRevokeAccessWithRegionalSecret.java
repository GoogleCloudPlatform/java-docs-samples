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

package secretmanager.regionalsamples;

// [START secretmanager_iam_revoke_access_with_regional_secret]
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import java.io.IOException;

public class IamRevokeAccessWithRegionalSecret {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    
    // Your GCP project ID.
    String projectId = "your-project-id";
    // Location of the secret.
    String locationId = "your-location-id";
    // Resource ID of the secret to revoke access to.
    String secretId = "your-secret-id";
    // IAM member, such as a user group or service account you want to revoke access.
    String member = "user:foo@example.com";
    iamRevokeAccessWithRegionalSecret(projectId, locationId, secretId, member);
  }

  // Revoke a member access to a particular secret.
  public static Policy iamRevokeAccessWithRegionalSecret(
      String projectId, String locationId, String secretId, String member)
      throws IOException {
    
    // Endpoint to call the regional secret manager sever
    String apiEndpoint = String.format("secretmanager.%s.rep.googleapis.com:443", locationId);
    SecretManagerServiceSettings secretManagerServiceSettings =
        SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (SecretManagerServiceClient client = 
        SecretManagerServiceClient.create(secretManagerServiceSettings)) {
      // Build the name from the version.
      SecretName secretName = 
          SecretName.ofProjectLocationSecretName(projectId, locationId, secretId);

      // Request the current IAM policy.
      Policy policy =
          client.getIamPolicy(
              GetIamPolicyRequest.newBuilder().setResource(secretName.toString()).build());

      // Search through bindings and remove matches.
      String roleToFind = "roles/secretmanager.secretAccessor";
      for (Binding binding : policy.getBindingsList()) {
        if (binding.getRole() == roleToFind && binding.getMembersList().contains(member)) {
          binding.getMembersList().remove(member);
        }
      }

      // Save the updated IAM policy.
      Policy updatedPolicy = client.setIamPolicy(
          SetIamPolicyRequest.newBuilder()
              .setResource(secretName.toString())
              .setPolicy(policy)
              .build());

      System.out.printf("Updated IAM policy for %s\n", secretId);

      return updatedPolicy;
    }
  }
}
// [END secretmanager_iam_revoke_access_with_regional_secret]
