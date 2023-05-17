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

// [START iam_query_testable_permissions]
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.cloud.iam.admin.v1.IAMClient.QueryTestablePermissionsPagedResponse;
import com.google.iam.admin.v1.QueryTestablePermissionsRequest;
import java.io.IOException;

public class QueryTestablePermissions {
  public static void main(String[] args) {
    // TODO(developer): Replace the variable before running the sample.
    String projectId = "your-project-id";

    queryTestablePermissions(projectId);
  }

  public static void queryTestablePermissions(String projectId) {
    // Full resource name can take one of the following forms:
    // cloudresourcemanager.googleapis.com/projects/PROJECT_ID
    // cloudresourcemanager.googleapis.com/organizations/NUMERIC_ID
    String fullResourceName = "//cloudresourcemanager.googleapis.com/projects/" + projectId;

    QueryTestablePermissionsRequest queryTestablePermissionsRequest =
        QueryTestablePermissionsRequest.newBuilder().setFullResourceName(fullResourceName).build();

    try (IAMClient iamClient = IAMClient.create()) {
      QueryTestablePermissionsPagedResponse queryTestablePermissionsPagedResponse =
          iamClient.queryTestablePermissions(queryTestablePermissionsRequest);
      queryTestablePermissionsPagedResponse
          .iterateAll()
          .forEach(permission -> System.out.println(permission.getName()));
    } catch (NotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
// [END iam_query_testable_permissions]
