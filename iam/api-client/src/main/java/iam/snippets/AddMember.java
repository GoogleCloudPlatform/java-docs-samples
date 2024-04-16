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

// [START iam_modify_policy_add_member]
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;

import java.util.ArrayList;
import java.util.List;

public class AddMember {
  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your policy, GetPolicy.getPolicy(projectId, serviceAccount)".
    Policy policy = Policy.newBuilder().build();
    addMember(policy);
  }

  // Adds a member to a preexisting role.
  public static void addMember(Policy policy) {
    // policy = GetPolicy.getPolicy(String projectId, String serviceAccount);
    String role = "roles/existing-role";
    String member = "user:member-to-add@example.com";

    List<Binding> newBindingsList = new ArrayList<>();

    for (Binding b : policy.getBindingsList()) {
      if (b.getRole().equals(role)) {
        newBindingsList.add(b.toBuilder().addMembers(member).build());
        System.out.println("Member " + member + " added to role " + role);
      } else {
        newBindingsList.add(b);
      }
    }
    //use new Policy
    policy.toBuilder()
            .clearBindings()
            .addAllBindings(newBindingsList)
            .build();
  }
}
// [END iam_modify_policy_add_member]
