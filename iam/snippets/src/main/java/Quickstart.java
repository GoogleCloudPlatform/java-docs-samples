/* Copyright 2020 Google LLC
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

// [START iam_quickstart]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quickstart {

  public static void main(String[] args) throws IOException {
    // TODO: Replace with your project ID.
    String projectId = "your-project";
    // TODO: Replace with your service account name.
    String serviceAccount = "your-service-account";
    // TODO: Replace with the ID of your member in the form "user:member@example.com"
    String member = "your-member";
    // The role to be granted.
    String role = "roles/logging.logWriter";

    quickstart(projectId, serviceAccount, member, role);
  }

  // Creates new policy and adds binding.
  // Checks if changes are present and removes policy.
  public static void quickstart(String projectId, String serviceAccount,
                                String member, String role) throws IOException {

    // Construct the service account email.
    // You can modify the ".iam.gserviceaccount.com" to match the name of the service account
    // to use for authentication.
    serviceAccount = serviceAccount + "@" + projectId + ".iam.gserviceaccount.com";

    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {

      // Grants your member the "Log writer" role for your project.
      addBinding(iamClient, projectId, serviceAccount, member, role);

      // Get the project's policy and print all members with the "Log Writer" role
      Policy policy = getPolicy(iamClient, projectId, serviceAccount);

      Binding binding = null;
      List<Binding> bindings = policy.getBindingsList();

      for (Binding b : bindings) {
        if (b.getRole().equals(role)) {
          binding = b;
          break;
        }
      }

      System.out.println("Role: " + binding.getRole());
      System.out.print("Members: ");

      for (String m : binding.getMembersList()) {
        System.out.print("[" + m + "] ");
      }
      System.out.println();

      // Removes member from the "Log writer" role.
      removeMember(iamClient, projectId, serviceAccount, member, role);
    }
  }

  public static void addBinding(IAMClient iamClient, String projectId, String serviceAccount,
                                String member, String role) {
    // Gets the project's policy.
    Policy policy = getPolicy(iamClient, projectId, serviceAccount);

    // If policy is not retrieved, return early.
    if (policy == null) {
      return;
    }

    Policy.Builder updatedPolicy = policy.toBuilder();

    // Get the binding if present in the policy.
    Binding binding = null;
    for (Binding b : updatedPolicy.getBindingsList()) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }

    if (binding != null) {
      // If binding already exists, adds member to binding.
      binding.getMembersList().add(member);
    } else {
      // If binding does not exist, adds binding to policy.
      binding = Binding.newBuilder()
              .setRole(role)
              .addMembers(member)
              .build();
      updatedPolicy.addBindings(binding);
    }

    // Sets the updated policy.
    setPolicy(iamClient, projectId, serviceAccount, updatedPolicy.build());
  }

  public static void removeMember(IAMClient iamClient, String projectId, String serviceAccount,
                                  String member, String role) {
    // Gets the project's policy.
    Policy.Builder policy = getPolicy(iamClient, projectId, serviceAccount).toBuilder();

    // Removes the member from the role.
    Binding binding = null;
    for (Binding b : policy.getBindingsList()) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }

    if (binding != null && binding.getMembersList().contains(member)) {
      List<String> newMemberList = new ArrayList<>(binding.getMembersList());
      newMemberList.remove(member);

      Binding newBinding = binding.toBuilder().clearMembers()
              .addAllMembers(newMemberList)
              .build();
      List<Binding> newBindingList = new ArrayList<>(policy.getBindingsList());
      newBindingList.remove(binding);

      if (!newBinding.getMembersList().isEmpty()) {
        newBindingList.add(newBinding);
      }

      policy.clearBindings()
              .addAllBindings(newBindingList);
    }

    // Sets the updated policy.
    setPolicy(iamClient, projectId, serviceAccount, policy.build());
  }

  public static Policy getPolicy(IAMClient iamClient, String projectId, String serviceAccount) {
    // Gets the project's policy by calling the
    // IAMClient API.
    GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
            .setResource(ServiceAccountName.of(projectId, serviceAccount).toString())
            .build();
    return iamClient.getIamPolicy(request);
  }

  private static void setPolicy(IAMClient iamClient, String projectId,
                                String serviceAccount, Policy policy) {
    List<String> paths = Arrays.asList("bindings", "etag");
    // Sets a project's policy.
    SetIamPolicyRequest request = SetIamPolicyRequest.newBuilder()
            .setResource(ServiceAccountName.of(projectId, serviceAccount).toString())
            .setPolicy(policy)
            // A FieldMask specifying which fields of the policy to modify. Only
            // the fields in the mask will be modified. If no mask is provided, the
            // following default mask is used:
            // `paths: "bindings, etag"`
            .setUpdateMask(FieldMask.newBuilder().addAllPaths(paths).build())
            .build();
    iamClient.setIamPolicy(request);
  }
}
// [END iam_quickstart]
