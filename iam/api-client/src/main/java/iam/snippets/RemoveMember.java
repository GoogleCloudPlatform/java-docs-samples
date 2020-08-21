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
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.Policy;
import java.util.List;

public class RemoveMember {

  // Removes member from a role; removes binding if binding contains 0 members.
  public static void removeMember(Policy policy) {
    // policy = service.Projects.GetIAmPolicy(new GetIamPolicyRequest(), your-project-id).Execute();

    String role = "roles/existing-role";
    String member = "user:member-to-remove@example.com";

    List<Binding> bindings = policy.getBindings();
    Binding binding = null;
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        binding = b;
      }
    }
    if (binding.getMembers().contains(member)) {
      binding.getMembers().remove(member);
      System.out.println("Member " + member + " removed from " + role);
      if (binding.getMembers().isEmpty()) {
        policy.getBindings().remove(binding);
      }
      return;
    }

    System.out.println("Role not found in policy; member not removed");
    return;
  }
}
// [END iam_modify_policy_remove_member]
