/* Copyright 2024 Google LLC
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

// [START iam_get_policy]
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.iam.admin.v1.ProjectName;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import java.io.IOException;

public class GetProjectPolicy {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace the variables before running the sample.
    // TODO: Replace with your project ID.
    String projectId = "your-project-id";

    getProjectPolicy(projectId);
  }

  // Gets a project's policy.
  public static Policy getProjectPolicy(String projectId) throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (ProjectsClient projectsClient = ProjectsClient.create()) {
      GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
              .setResource(ProjectName.of(projectId).toString())
              .build();
      return projectsClient.getIamPolicy(request);
    }
  }
}
// [END iam_get_policy]
