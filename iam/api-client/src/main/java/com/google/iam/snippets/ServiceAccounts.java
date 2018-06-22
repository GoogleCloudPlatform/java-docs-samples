// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START iam_quickstart]
package com.google.iam.snippets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.*;
import com.google.api.services.iam.v1.model.*;

public class ServiceAccounts  {

    private final Iam service;

    public ServiceAccounts() throws Exception {
        GoogleCredential credential = GoogleCredential.getApplicationDefault()
        .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
    
        service = new Iam.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), 
            JacksonFactory.getDefaultInstance(), 
            credential).build();
    }

    // [START iam_create_service_account]
    public void CreateServiceAccount( String projectId, String name,
        String displayName) throws IOException {

        ServiceAccount account = new ServiceAccount();
        account.setDisplayName(displayName);
        CreateServiceAccountRequest request = new CreateServiceAccountRequest();
        request.setAccountId(name);
        request.setServiceAccount(account);
        
        account = service.projects().serviceAccounts().create(
            "projects/" + projectId, request).execute();

        System.out.println("Created service account: " + account.getEmail());
    }
    // [END iam_create_service_account]

    // [START iam_list_service_accounts]
    public void ListServiceAccounts( String projectId ) throws IOException {
  
        ListServiceAccountsResponse response = service.projects()
            .serviceAccounts().list("projects/" + projectId).execute();
        List<ServiceAccount> serviceAccounts = response.getAccounts();

        for (ServiceAccount account : serviceAccounts) {
            System.out.println(account.getEmail());
        }
    }
    // [END iam_list_service_accounts]

    // [START iam_rename_service_account]
    public void RenameServiceAccount( String email, 
        String newDisplayName) throws IOException {

        // First, get a service account using List() or Get()
        ServiceAccount account = service.projects().serviceAccounts().get(
            "projects/-/serviceAccounts/" + email).execute();

        // Then you can update the display name
        account.setDisplayName(newDisplayName);
        service.projects().serviceAccounts().update(
            account.getName(), account).execute();

        System.out.println("Updated display name for " + account.getName() +
            " to: " + newDisplayName);
    }
    // [END iam_rename_service_account]

    // [START iam_delete_service_account]
    public void DeleteServiceAccount( String email ) throws IOException { 

        service.projects().serviceAccounts().delete(
            "projects/-/serviceAccounts/" + email).execute();

        System.out.println("Deleted service account: " + email);
    }
    // [END iam_delete_service_account]
}
