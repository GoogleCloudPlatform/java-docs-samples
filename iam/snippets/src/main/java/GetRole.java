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

// [START iam_get_role]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.GetRoleRequest;
import com.google.iam.admin.v1.Role;
import java.io.IOException;

/** Get role metadata. Specifically, printing out role permissions. */
public class GetRole {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variable before running the sample.
    String roleId = "a unique identifier (e.g. testViewer)";

    getRole(roleId);
  }

  public static void getRole(String roleId) throws IOException {
    GetRoleRequest getRoleRequest = GetRoleRequest.newBuilder().setName(roleId).build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      Role role = iamClient.getRole(getRoleRequest);
      role.getIncludedPermissionsList().forEach(permission -> System.out.println(permission));
    }
  }
}
// [END iam_get_role]
