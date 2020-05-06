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

package com.google.iam.snippets;
// [START iam_quickstart_v2]

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.cloudresourcemanager.model.SetIamPolicyRequest;
import com.google.api.services.iam.v1.IamScopes;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class QuickstartV2 {

  public static void main(String[] args) {
    // TODO: Replace with your project ID.
    String projectId = "your-project";
    // TODO: Replace with the ID of your member in the form "member:user@example.com"
    String member = "your-member";
    // The role to be granted.
    String role = "roles/logging.logWriter";

    // Initializes the Cloud Resource Manager service.
    CloudResourceManager crmService = null;
    try {
      crmService = initializeService();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
    }

    // Grants your member the "Log writer" role for your project.
    addBinding(crmService, projectId, member, role);

    // Get the project's policy and print all members with the "Log Writer" role
    Policy policy = getPolicy(crmService, projectId);
    Binding binding = null;
    List<Binding> bindings = policy.getBindings();
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }
    System.out.println("Role: " + binding.getRole());
    System.out.print("Members: ");
    for (String m : binding.getMembers()) {
      System.out.print("[" + m + "] ");
    }
    System.out.println();

    // Removes member from the "Log writer" role.
    removeMember(crmService, projectId, member, role);
  }

  public static CloudResourceManager initializeService()
      throws IOException, GeneralSecurityException {
    // Use the Application Default Credentials strategy for authentication. For more info, see:
    // https://cloud.google.com/docs/authentication/production#finding_credentials_automatically
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    // Creates the Cloud Resource Manager service object.
    CloudResourceManager service =
        new CloudResourceManager.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("service-accounts")
            .build();
    return service;
  }

  public static void addBinding(
      CloudResourceManager crmService, String projectId, String member, String role) {

    // Gets the project's policy.
    Policy policy = getPolicy(crmService, projectId);

    // If binding already exists, adds member to binding.
    List<Binding> bindings = policy.getBindings();
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        b.getMembers().add(member);
        break;
      }
    }

    // If binding does not exist, adds binding to policy.
    Binding binding = new Binding();
    binding.setRole(role);
    binding.setMembers(Collections.singletonList(member));
    policy.getBindings().add(binding);

    // Set the updated policy
    setPolicy(crmService, projectId, policy);
  }

  public static void removeMember(
      CloudResourceManager crmService, String projectId, String member, String role) {
    // Gets the project's policy.
    Policy policy = getPolicy(crmService, projectId);

    // Removes the member from the role.
    List<Binding> bindings = policy.getBindings();
    Binding binding = null;
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }
    if (binding.getMembers().contains(member)) {
      binding.getMembers().remove(member);
      if (binding.getMembers().isEmpty()) {
        policy.getBindings().remove(binding);
      }
    }

    // Sets the updated policy.
    setPolicy(crmService, projectId, policy);
  }

  public static Policy getPolicy(CloudResourceManager crmService, String projectId) {
    // Gets the project's policy by calling the
    // Cloud Resource Manager Projects API.
    Policy policy = null;
    try {
      GetIamPolicyRequest request = new GetIamPolicyRequest();
      policy = crmService.projects().getIamPolicy(projectId, request).execute();
    } catch (IOException e) {
      System.out.println("Unable to get policy: \n" + e.toString());
    }
    return policy;
  }

  private static void setPolicy(CloudResourceManager crmService, String projectId, Policy policy) {
    // Sets the project's policy by calling the
    // Cloud Resource Manager Projects API.
    try {
      SetIamPolicyRequest request = new SetIamPolicyRequest();
      request.setPolicy(policy);
      crmService.projects().setIamPolicy(projectId, request).execute();
    } catch (IOException e) {
      System.out.println("Unable to set policy: \n" + e.toString());
    }
  }
}
// [END iam_quickstart_v2]
