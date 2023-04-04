/*
 * Copyright 2022 Google LLC
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

import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateSecretWithUserManagedReplication {

  public static void createSecret() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String secretId = "your-secret-id";
    // TODO(developer): Replace these locations with the locations where replication is needed.
    List<String> locations = Arrays.asList("us-east1", "us-east4", "us-west1");
    createSecret(projectId, secretId, locations);
  }

  // Create a new secret with user managed replication.
  public static void createSecret(
      String projectId, String secretId, List<String> locations) throws IOException {
    // Initialize the client that will be used to send requests. This client only needs to be 
    // created once, and can be reused for multiple requests. After completing all of your requests,
    // call the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the parent name from the project.
      ProjectName projectName = ProjectName.of(projectId);

      // Set replication.
      Replication.UserManaged.Builder replication = Replication.UserManaged.newBuilder();
      for (String location : locations) {
        replication.addReplicas(
            Replication.UserManaged.Replica.newBuilder().setLocation(location).build());
      }

      // Build the secret to create.
      Secret secret =
          Secret.newBuilder()
              .setReplication(
                  Replication.newBuilder()
                      .setUserManaged(replication.build())
                      .build())
              .build();

      // Create the secret.
      Secret createdSecret = client.createSecret(projectName, secretId, secret);
      System.out.printf("Created secret %s\n", createdSecret.getName());
    }
  }
}
