/*
 * Copyright 2023 Google LLC
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

// [START iam_undelete_role]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.Role;
import com.google.iam.admin.v1.UndeleteRoleRequest;
import java.io.IOException;

/**
 * Undelete a role to return it to its previous state. Undeleting only works on roles that were
 * deleted in the past 7 days.
 */
public class UndeleteRole {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // Role ID must point to a role that was deleted in the past 7 days.
    String projectId = "your-project-id";
    String roleId = "a unique identifier (e.g. testViewer)";

    undeleteRole(projectId, roleId);
  }

  public static void undeleteRole(String projectId, String roleId) throws IOException {
    String roleName = "projects/" + projectId + "/roles/" + roleId;
    UndeleteRoleRequest undeleteRoleRequest =
        UndeleteRoleRequest.newBuilder().setName(roleName).build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      Role result = iamClient.undeleteRole(undeleteRoleRequest);
      System.out.println("Undeleted role:\n" + result);
    }
  }
}
// [END iam_undelete_role]
