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

// [START iam_modify_policy_add_role]

import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.util.Collections;
import java.util.List;

public class AddBinding {
  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your policy: GetPolicy.getPolicy(projectId, serviceAccount).
    Policy policy = Policy.newBuilder().build();
    // TODO: Replace with your role.
    String role = "roles/role-to-add";
    // TODO: Replace with your members.
    List<String> members = Collections.singletonList("user:member-to-add@example.com");

    addBinding(policy, role, members);
  }

  // Adds a member to a role.
  public static Policy addBinding(Policy policy, String role, List<String> members) {
    Binding binding = Binding.newBuilder()
            .setRole(role)
            .addAllMembers(members)
            .build();

    // Update bindings for the policy.
    Policy updatedPolicy = policy.toBuilder().addBindings(binding).build();

    System.out.println("Added binding: " + updatedPolicy.getBindingsList());

    return updatedPolicy;
  }
}
// [END iam_modify_policy_add_role]
