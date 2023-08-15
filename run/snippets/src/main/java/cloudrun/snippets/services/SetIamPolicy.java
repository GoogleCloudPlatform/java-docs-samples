/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.services;

// [START cloudrun_set_iam_policy]
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class SetIamPolicy {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service";
    String member = "user:user@domain.com"; // Learn more about member values https://cloud.google.com/iam/docs/overview#concepts_related_identity
    String role = "roles/run.invoker"; // Lear more about role values https://cloud.google.com/run/docs/reference/iam/roles
    setIamPolicy(projectId, location, serviceId, member, role);
  }

  // You must have the run.services.setIamPolicy permission to configure authentication on a Cloud Run service.
  public static void setIamPolicy(String projectId, String location, String serviceId, String member, String role) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ServicesClient servicesClient = ServicesClient.create()) {
      // Define IAM policy binding
      // https://cloud.google.com/iam/docs/reference/rest/v1/Policy
      Binding binding =
          Binding.newBuilder()
              .addMembers(member)
              .setRole(role)
              .build();
      Policy policy = Policy.newBuilder().addBindings(binding).build();

      SetIamPolicyRequest request =
          SetIamPolicyRequest.newBuilder()
              .setResource(ServiceName.of(projectId, location, serviceId).toString())
              .setPolicy(policy)
              .build();
      // Send request
      Policy response = servicesClient.setIamPolicy(request);
      System.out.println("IAM Policy bindings for service, " + serviceId + ":");
      // Example usage of the Policy object
      for (Binding updatedBinding : response.getBindingsList()) {
        System.out.println("Role: " + updatedBinding.getRole());
        System.out.println("Members: " + updatedBinding.getMembersList());
      }
    }
  }
}
// [END cloudrun_set_iam_policy]
