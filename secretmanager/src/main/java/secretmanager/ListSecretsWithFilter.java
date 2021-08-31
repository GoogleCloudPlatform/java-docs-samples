/*
 * Copyright 2021 Google LLC
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

package secretmanager;

// [START secretmanager_list_secrets_with_filter]
import com.google.cloud.secretmanager.v1.ListSecretsRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient.ListSecretsPagedResponse;
import java.io.IOException;

public class ListSecretsWithFilter {

  public static void listSecrets() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    // Follow https://cloud.google.com/secret-manager/docs/filtering
    // for filter syntax and examples.
    String filter = "name:your-secret-substring AND expire_time<2022-01-01T00:00:00Z";
    listSecrets(projectId, filter);
  }

  // List all secrets for a project
  public static void listSecrets(String projectId, String filter) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name.
      ProjectName projectName = ProjectName.of(projectId);

      // Get filtered secrets.
      ListSecretsRequest request =
          ListSecretsRequest.newBuilder()
              .setParent(projectName.toString())
              .setFilter(filter)
              .build();

      ListSecretsPagedResponse pagedResponse = client.listSecrets(request);

      // List all secrets.
      pagedResponse
          .iterateAll()
          .forEach(
              secret -> {
                System.out.printf("Secret %s\n", secret.getName());
              });
    }
  }
}
// [END secretmanager_list_secrets_with_filter]
