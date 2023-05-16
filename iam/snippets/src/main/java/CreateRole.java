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

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.CreateRoleRequest;
import com.google.iam.admin.v1.Role;
import com.google.iam.admin.v1.Role.RoleLaunchStage;
import java.io.IOException;
import java.util.Arrays;

// [START iam_create_role]
public class CreateRole {
  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    String projectId = "your-project-id";
    String title = "Role Title";
    String description = "A description";
    Iterable<String> includedPermissions = Arrays.asList("roles/iam.roleViewer");
    String roleId = "your-role-id";

    createRole(projectId, title, description, includedPermissions, roleId);
  }

  public static void createRole(
      String projectId,
      String title,
      String description,
      Iterable<String> includedPermissions,
      String roleId) {
    Role.Builder roleBuilder =
        Role.newBuilder()
            .setTitle(title)
            .setDescription(description)
            .addAllIncludedPermissions(includedPermissions)
            .setStage(RoleLaunchStage.BETA);

    CreateRoleRequest createRoleRequest =
        CreateRoleRequest.newBuilder()
            .setParent("projects/" + projectId)
            .setRoleId(roleId)
            .setRole(roleBuilder)
            .build();

    try (IAMClient iamClient = IAMClient.create()) {
      Role result = iamClient.createRole(createRoleRequest);
      System.out.println("Created role: " + result.getName());
    } catch (AlreadyExistsException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
// [END iam_create_role]
