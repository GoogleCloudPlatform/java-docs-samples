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

// [START iam_quickstart_v2]

package com.google.iam.snippets;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.*;
import com.google.api.services.iam.v1.IamScopes;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuickstartV2 {

  public static void main(String[] args) {
    // TODO: Replace with your project ID.
    String projectId = "your-project";
    // TODO: Replace with the ID of the service account used in this quickstart in
    // the form "serviceAccount:[service-account-id]@[project-id].iam.gserviceaccount.com"
    String member = "your-service-account";
    // The role to be granted.
    String role = "roles/logging.logWriter";
    // All permissions contained in the role to be granted.
    List<String> rolePermissions = Arrays.asList("logging.logEntries.create");

    // Initializes the Cloud Resource Manager service.
    CloudResourceManager crmService = null;
    try {
      crmService = initializeService();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
    }

    // Grants your member the "Log writer" role for your project.
    addBinding(crmService, projectId, member, role);

    // Tests if the member has the permissions granted by the role.
    List<String> grantedPermissions =
        testPermissions(crmService, projectId, rolePermissions);
    // Prints the role permissions held by the member.
    for (String p : grantedPermissions) {
      System.out.println(p);
    }

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

  // Adds a member to a role.
  public static void addBinding(
      CloudResourceManager crmService, String projectId, String member, String role) {

    // Gets the project's policy by calling the
    // Cloud Resource Manager Projects API.
    Policy policy = null;
    try {
      GetIamPolicyRequest request = new GetIamPolicyRequest();
      policy = crmService.projects().getIamPolicy(projectId, request).execute();
    } catch (IOException e) {
      System.out.println("Unable to get policy: \n" + e.toString());
    }

    // If binding already exists, adds member to binding.
    List<Binding> bindings = policy.getBindings();
    for (Binding b : bindings) {
      if (b.getRole() == role) {
        b.getMembers().add(member);
        return;
      }
    }

    // If binding does not exist, adds binding to policy.
    Binding binding = new Binding();
    binding.setRole(role);
    binding.setMembers(Arrays.asList(member));
    policy.getBindings().add(binding);

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

  // Tests if the caller has the listed permissions.
  public static List<String> testPermissions(
      CloudResourceManager crmService,
      String projectId,
      List<String> rolePermissions) {

    // Tests the member's permissions by calling the
    // Cloud Resource Manager Projects API.
    TestIamPermissionsRequest requestBody =
        new TestIamPermissionsRequest().setPermissions(rolePermissions);
    try {
      TestIamPermissionsResponse testIamPermissionsResponse =
          crmService.projects().testIamPermissions(projectId, requestBody).execute();

      return testIamPermissionsResponse.getPermissions();
    } catch (IOException e) {
      System.out.println("Unable to test permissions: \n" + e.toString());
      return null;
    }
  }

  // Removes a member from a role.
  public static void removeMember(
      CloudResourceManager crmService, String projectId, String member, String role) {
    // Gets the project's policy by calling the
    // Cloud Resource Manager Projects API.
    Policy policy = null;
    try {
      GetIamPolicyRequest request = new GetIamPolicyRequest();
      policy = crmService.projects().getIamPolicy(projectId, request).execute();
    } catch (IOException e) {
      System.out.println("Unable to get policy: \n" + e.toString());
    }

    // Removes the member from the role.
    List<Binding> bindings = policy.getBindings();

    for (Binding b : bindings) {
      if (b.getRole() == role) {
        if (b.getMembers().contains(member)) {
          b.getMembers().remove(member);
          System.out.println("Member " + member + " removed from " + role);
        }
        if (b.getMembers().size() == 0) {
          policy.getBindings().remove(b);
        }
      }
    }

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
