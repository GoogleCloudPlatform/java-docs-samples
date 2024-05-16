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

// [START iam_modify_policy_add_member]
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.util.ArrayList;
import java.util.List;

public class AddMember {
  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount).
    Policy policy = Policy.newBuilder().build();
    // TODO: Replace with your role.
    String role = "roles/existing-role";
    // TODO: Replace with your member.
    String member = "user:member-to-add@example.com";

    addMember(policy, role, member);
  }

  // Adds a member to a pre-existing role.
  public static Policy addMember(Policy policy, String role, String member) {
    List<Binding> newBindingsList = new ArrayList<>();

    for (Binding b : policy.getBindingsList()) {
      if (b.getRole().equals(role)) {
        newBindingsList.add(b.toBuilder().addMembers(member).build());
      } else {
        newBindingsList.add(b);
      }
    }

    // Update the policy to add the member.
    Policy updatedPolicy = policy.toBuilder()
            .clearBindings()
            .addAllBindings(newBindingsList)
            .build();

    System.out.println("Added member: " + updatedPolicy.getBindingsList());

    return updatedPolicy;
  }
}
// [END iam_modify_policy_add_member]
