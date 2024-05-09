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

// [START iam_modify_policy_remove_member]
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoveMember {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount).
    Policy policy = Policy.newBuilder().build();
    // TODO: Replace with your role.
    String role = "roles/existing-role";
    // TODO: Replace with your member.
    String member = "user:member-to-add@example.com";

    removeMember(policy, role, member);
  }

  // Removes member from a role; removes binding if binding contains no members.
  public static Policy removeMember(Policy policy, String role, String member) {
    // Creating new builder with all values copied from origin policy
    Policy.Builder policyBuilder = policy.toBuilder();

    // Getting binding with suitable role.
    Binding binding = null;
    for (Binding b : policy.getBindingsList()) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }

    if (binding != null && binding.getMembersList().contains(member)) {
      List<String> newMemberList = new ArrayList<>(binding.getMembersList());
      // Removing member from a role
      newMemberList.remove(member);

      System.out.println("Member " + member + " removed from " + role);

      // Adding all remaining members to create new binding
      Binding newBinding = binding.toBuilder()
              .clearMembers()
              .addAllMembers(newMemberList)
              .build();

      List<Binding> newBindingList = new ArrayList<>(policyBuilder.getBindingsList());

      // Removing old binding to replace with new one
      newBindingList.remove(binding);

      // If binding has no more members, binding will not be added
      if (!newBinding.getMembersList().isEmpty()) {
        newBindingList.add(newBinding);
      }

      // Update the policy to remove the member.
      policyBuilder.clearBindings()
              .addAllBindings(newBindingList);
    }

    Policy updatedPolicy = policyBuilder.build();

    System.out.println("Exising members: " + updatedPolicy.getBindingsList());

    return updatedPolicy;
  }
}
// [END iam_modify_policy_remove_member]
