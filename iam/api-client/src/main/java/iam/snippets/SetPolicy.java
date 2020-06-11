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

// [START iam_set_policy]
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.cloudresourcemanager.model.SetIamPolicyRequest;
import com.google.api.services.iam.v1.IamScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class SetPolicy {

  // Sets a project's policy.
  public static void setPolicy(Policy policy, String projectId) {
    // policy = service.Projects.GetIAmPolicy(new GetIamPolicyRequest(), your-project-id).Execute();
    // projectId = "my-project-id"

    CloudResourceManager service = null;
    try {
      service = createCloudResourceManagerService();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
      return;
    }

    try {
      SetIamPolicyRequest request = new SetIamPolicyRequest();
      request.setPolicy(policy);
      Policy response = service.projects().setIamPolicy(projectId, request).execute();
      System.out.println("Policy set: " + response.toString());
    } catch (IOException e) {
      System.out.println("Unable to set policy: \n" + e.toString());
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
// [END iam_set_policy]
