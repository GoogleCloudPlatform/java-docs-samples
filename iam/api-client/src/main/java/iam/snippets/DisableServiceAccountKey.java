/* Copyright 2021 Google LLC
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

// [START iam_disable_service_account_key]

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.DisableServiceAccountKeyRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


public class DisableServiceAccountKey {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the below variables before running.
    String projectId = "gcloud-project-id";
    String serviceAccountName = "service-account-name";
    String serviceAccountKeyName = "service-account-key-name";

    disableServiceAccountKey(projectId, serviceAccountName, serviceAccountKeyName);
  }

  // Disables a service account key.
  public static void disableServiceAccountKey(String projectId, String serviceAccountName,
      String serviceAccountKeyName) {
    // Initialize the IAM service.
    Iam service = null;
    try {
      service = initService();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e);
      return;
    }

    String serviceAccountEmail = serviceAccountName + "@" + projectId + ".iam.gserviceaccount.com";

    try {
      DisableServiceAccountKeyRequest
          disableServiceAccountKeyRequest = new DisableServiceAccountKeyRequest();
      // Use the IAM service to disable the service account key.
      service
          .projects()
          .serviceAccounts()
          .keys()
          .disable(String
              .format("projects/%s/serviceAccounts/%s/keys/%s", projectId, serviceAccountEmail,
                  serviceAccountKeyName), disableServiceAccountKeyRequest)
          .execute();

      System.out.println("Disabled service account key: " + serviceAccountKeyName);
    } catch (IOException e) {
      System.out.println("Failed to disable service account key: \n" + e);
    }
  }

  private static Iam initService() throws GeneralSecurityException, IOException {
    /* Use the Application Default Credentials strategy for authentication. For more info, see:
     https://cloud.google.com/docs/authentication/production#finding_credentials_automatically */
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    // Initialize the IAM service, which can be used to send requests to the IAM API.
    return new Iam.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(),
        new HttpCredentialsAdapter(credential))
        .setApplicationName("service-accounts")
        .build();
  }
}
// [END iam_disable_service_account_key]

