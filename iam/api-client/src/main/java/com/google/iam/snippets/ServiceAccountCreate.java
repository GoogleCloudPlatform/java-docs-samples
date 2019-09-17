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

// [START iam_create_service_account]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.util.Collections;


public class ServiceAccountCreate {

    public static ServiceAccount createServiceAccount(String projectId, String name, String displayName) throws Exception {

        GoogleCredential credential = GoogleCredential.getApplicationDefault()
          .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
  
        Iam service = new Iam.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
          credential).setApplicationName("service-accounts").build();

        ServiceAccount serviceAccount = new ServiceAccount();
        serviceAccount.setDisplayName(displayName);
        CreateServiceAccountRequest request = new CreateServiceAccountRequest();
        request.setAccountId(name);
        request.setServiceAccount(serviceAccount);
    
        serviceAccount = service.projects().serviceAccounts().create("projects/" + projectId, request).execute();
    
        System.out.println("Created service account: " + serviceAccount.getEmail());
        return serviceAccount;
      }
}
// [END iam_create_service_account]
