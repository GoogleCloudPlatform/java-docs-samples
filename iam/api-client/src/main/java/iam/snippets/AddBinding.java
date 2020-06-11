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

// [START iam_modify_policy_add_binding]
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.Policy;
import java.util.ArrayList;
import java.util.List;

public class AddBinding {

  // Adds a member to a role with no previous members.
  public static void addBinding(Policy policy) {
    // policy = service.Projects.GetIAmPolicy(new GetIamPolicyRequest(), your-project-id).Execute();

    String role = "roles/role-to-add";
    List<String> members = new ArrayList<String>();
    members.add("user:member-to-add@example.com");

    Binding binding = new Binding();
    binding.setRole(role);
    binding.setMembers(members);

    policy.getBindings().add(binding);
    System.out.println("Added binding: " + binding.toString());
  }
}
// [END iam_modify_policy_add_binding]
