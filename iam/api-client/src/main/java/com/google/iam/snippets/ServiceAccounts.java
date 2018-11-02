/* Copyright 2018 Google LLC
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

package com.google.iam.snippets;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ListServiceAccountsResponse;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ServiceAccounts {

  private final Iam service;

  public ServiceAccounts() throws Exception {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    service =
        new Iam.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("service-accounts")
            .build();
  }

  // [START iam_create_service_account]
  public ServiceAccount createServiceAccount(String projectId, String name, String displayName)
      throws IOException {

    ServiceAccount serviceAccount = new ServiceAccount();
    serviceAccount.setDisplayName(displayName);
    CreateServiceAccountRequest request = new CreateServiceAccountRequest();
    request.setAccountId(name);
    request.setServiceAccount(serviceAccount);

    serviceAccount =
        service.projects().serviceAccounts().create("projects/" + projectId, request).execute();

    System.out.println("Created service account: " + serviceAccount.getEmail());
    return serviceAccount;
  }
  // [END iam_create_service_account]

  // [START iam_list_service_accounts]
  public List<ServiceAccount> listServiceAccounts(String projectId) throws IOException {

    ListServiceAccountsResponse response =
        service.projects().serviceAccounts().list("projects/" + projectId).execute();
    List<ServiceAccount> serviceAccounts = response.getAccounts();

    for (ServiceAccount account : serviceAccounts) {
      System.out.println("Name: " + account.getName());
      System.out.println("Display Name: " + account.getDisplayName());
      System.out.println("Email: " + account.getEmail());
      System.out.println();
    }
    return serviceAccounts;
  }
  // [END iam_list_service_accounts]

  // [START iam_rename_service_account]
  public ServiceAccount renameServiceAccount(String email, String newDisplayName)
      throws IOException {

    // First, get a service account using List() or Get()
    ServiceAccount serviceAccount =
        service.projects().serviceAccounts().get("projects/-/serviceAccounts/" + email).execute();

    // Then you can update the display name
    serviceAccount.setDisplayName(newDisplayName);
    service.projects().serviceAccounts().update(serviceAccount.getName(), serviceAccount).execute();

    System.out.println(
        "Updated display name for " + serviceAccount.getName() + " to: " + newDisplayName);
    return serviceAccount;
  }
  // [END iam_rename_service_account]

  // [START iam_delete_service_account]
  public void deleteServiceAccount(String email) throws IOException {

    service.projects().serviceAccounts().delete("projects/-/serviceAccounts/" + email).execute();

    System.out.println("Deleted service account: " + email);
  }
  // [END iam_delete_service_account]
}
