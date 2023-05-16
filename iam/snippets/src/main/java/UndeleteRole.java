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

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.Role;
import com.google.iam.admin.v1.UndeleteRoleRequest;
import java.io.IOException;

// [START iam_undelete_role]
public class UndeleteRole {

  public static void main(String[] args) {
    // TODO(developer): Replace the variables before running the sample.
    // Role ID must point to a deleted role within 7 days.
    String projectId = "your-project-id";
    String roleId = "your-role-id";

    undeleteRole(projectId, roleId);
  }

  public static void undeleteRole(String projectId, String roleId) {

    UndeleteRoleRequest undeleteRoleRequest =
        UndeleteRoleRequest.newBuilder()
            .setName("projects/" + projectId + "/roles/" + roleId)
            .build();

    try (IAMClient iamClient = IAMClient.create()) {
      Role result = iamClient.undeleteRole(undeleteRoleRequest);
      System.out.println("Undeleted role:\n" + result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
// [END iam_undelete_role]
