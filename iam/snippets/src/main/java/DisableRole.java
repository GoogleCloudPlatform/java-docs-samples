/*
 * Copyright 2024 Google LLC
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

// [START iam_disable_role]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.Role;
import com.google.iam.admin.v1.UpdateRoleRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class DisableRole {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // Role ID must point to an existing role.
    String projectId = "your-project-id";
    String roleId = "testRole";

    Role role = disableRole(projectId, roleId);
    System.out.println("Role name: " + role.getName());
    System.out.println("Role stage: " + role.getStage());
  }

  public static Role disableRole(String projectId, String roleId)
          throws IOException {
    String roleName = "projects/" + projectId + "/roles/" + roleId;
    Role role = Role.newBuilder()
                    .setName(roleName)
                    .setStage(Role.RoleLaunchStage.DISABLED)
                    .build();

    FieldMask fieldMask = FieldMask.newBuilder().addPaths("stage").build();
    UpdateRoleRequest updateRoleRequest =
              UpdateRoleRequest.newBuilder()
                      .setName(roleName)
                      .setRole(role)
                      .setUpdateMask(fieldMask)
                      .build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      return iamClient.updateRole(updateRoleRequest);
    }
  }
}
// [END iam_disable_role]
