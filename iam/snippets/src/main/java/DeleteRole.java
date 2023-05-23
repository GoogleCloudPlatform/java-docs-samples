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

// [START iam_delete_role]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DeleteRoleRequest;
import java.io.IOException;

/** Delete role. */
public class DeleteRole {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // Role ID must point to an existing role.
    String projectId = "your-project-id";
    String roleId = "a unique identifier (e.g. testViewer)";

    deleteRole(projectId, roleId);
  }

  public static void deleteRole(String projectId, String roleId) throws IOException {
    String roleName = "projects/" + projectId + "/roles/" + roleId;
    DeleteRoleRequest deleteRoleRequest = DeleteRoleRequest.newBuilder().setName(roleName).build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.deleteRole(deleteRoleRequest);
      System.out.println("Role deleted.");
    }
  }
}
// [END iam_delete_role]
