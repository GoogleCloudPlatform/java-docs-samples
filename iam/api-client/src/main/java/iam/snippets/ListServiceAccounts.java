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

// [START iam_list_service_accounts]

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.ListServiceAccountsResponse;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ListServiceAccounts {

  // Lists all service accounts for the current project.
  public static void listServiceAccounts(String projectId)
      throws GeneralSecurityException, IOException {
    // String projectId = "my-project-id"

    Iam service = initService();

    ListServiceAccountsResponse response =
        service.projects().serviceAccounts().list("projects/" + projectId).execute();
    List<ServiceAccount> serviceAccounts = response.getAccounts();

    for (ServiceAccount account : serviceAccounts) {
      System.out.println("Name: " + account.getName());
      System.out.println("Display Name: " + account.getDisplayName());
      System.out.println("Email: " + account.getEmail());
      System.out.println();
    }
  }

  private static Iam initService() throws GeneralSecurityException, IOException {
    // Use the Application Default Credentials strategy for authentication. For more info, see:
    // https://cloud.google.com/docs/authentication/production#finding_credentials_automatically
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
    // Initialize the IAM service, which can be used to send requests to the IAM API.
    Iam.Builder serviceBuilder = new Iam.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(),
        new HttpCredentialsAdapter(credential));

    return serviceBuilder
        .setApplicationName("service-accounts")
        .build();
  }
}
// [END iam_list_service_accounts]
