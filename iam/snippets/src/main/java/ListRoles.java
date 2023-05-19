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

// [START iam_list_roles]

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.cloud.iam.admin.v1.IAMClient.ListRolesPagedResponse;
import com.google.iam.admin.v1.ListRolesRequest;
import java.io.IOException;

/** List roles in a project. */
public class ListRoles {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variable before running the sample.
    String projectId = "your-project-id";

    listRoles(projectId);
  }

  public static void listRoles(String projectId) throws IOException {
    ListRolesRequest listRolesRequest =
        ListRolesRequest.newBuilder().setParent("projects/" + projectId).build();

    // Initialize client for sending requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IAMClient iamClient = IAMClient.create()) {
      ListRolesPagedResponse listRolesResponse = iamClient.listRoles(listRolesRequest);
      listRolesResponse.iterateAll().forEach(role -> System.out.println(role));
    }
  }
}
// [END iam_list_roles]
