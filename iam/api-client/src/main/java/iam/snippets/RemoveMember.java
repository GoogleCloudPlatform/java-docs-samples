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

// [START iam_modify_policy_remove_member]
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoveMember {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount)".
    Policy policy = Policy.newBuilder().build();
    removeMember(policy);
  }

  // Removes member from a role; removes binding if binding contains 0 members.
  public static void removeMember(Policy policy) {
    // policy = GetPolicy.getPolicy(String projectId, String serviceAccount);

    String role = "roles/existing-role";
    String member = "user:member-to-remove@example.com";

    Policy.Builder policyBuilder = policy.toBuilder();

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
      System.out.println("Member " + member + " removed from " + role);

      Binding newBinding = binding.toBuilder().clearMembers()
              .addAllMembers(newMemberList)
              .build();
      List<Binding> newBindingList = new ArrayList<>(policyBuilder.getBindingsList());
      newBindingList.remove(binding);

      if (!newBinding.getMembersList().isEmpty()) {
        newBindingList.add(newBinding);
      }

      policyBuilder.clearBindings()
              .addAllBindings(newBindingList);
    }

    System.out.println("Role not found in policy; member not removed");
  }
}
// [END iam_modify_policy_remove_member]
