/* Copyright 2019 Google LLC
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

package iam.snippets;

// [START iam_test_permissions]
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.TestIamPermissionsRequest;
import com.google.api.services.cloudresourcemanager.model.TestIamPermissionsResponse;
import com.google.api.services.iam.v1.IamScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestPermissions {

  // Tests if the caller has the listed permissions.
  public static void testPermissions(String projectId) {
    // projectId = "my-project-id"

    CloudResourceManager service = null;
    try {
      service = createCloudResourceManagerService();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
      return;
    }

    List<String> permissionsList =
        Arrays.asList("resourcemanager.projects.get", "resourcemanager.projects.delete");

    TestIamPermissionsRequest requestBody =
        new TestIamPermissionsRequest().setPermissions(permissionsList);
    try {
      TestIamPermissionsResponse testIamPermissionsResponse =
          service.projects().testIamPermissions(projectId, requestBody).execute();

      System.out.println(
          "Of the permissions listed in the request, the caller has the following: "
              + testIamPermissionsResponse.getPermissions().toString());
    } catch (IOException e) {
      System.out.println("Unable to test permissions: \n" + e.toString());
    }
  }

  public static CloudResourceManager createCloudResourceManagerService()
      throws IOException, GeneralSecurityException {
    // Use the Application Default Credentials strategy for authentication. For more info, see:
    // https://cloud.google.com/docs/authentication/production#finding_credentials_automatically
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    CloudResourceManager service =
        new CloudResourceManager.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credential))
            .setApplicationName("service-accounts")
            .build();
    return service;
  }
}
// [END iam_test_permissions]
