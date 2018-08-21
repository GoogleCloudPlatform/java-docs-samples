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
import com.google.api.services.iam.v1.model.CreateServiceAccountKeyRequest;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ServiceAccountKeys {

  private final Iam service;

  public ServiceAccountKeys() throws Exception {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    service =
        new Iam.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("service-account-keys")
            .build();
  }

  // [START iam_create_key]
  public ServiceAccountKey createKey(String serviceAccountEmail) throws IOException {

    ServiceAccountKey key =
        service
            .projects()
            .serviceAccounts()
            .keys()
            .create(
                "projects/-/serviceAccounts/" + serviceAccountEmail,
                new CreateServiceAccountKeyRequest())
            .execute();

    System.out.println("Created key: " + key.getName());
    return key;
  }
  // [END iam_create_key]

  // [START iam_list_keys]
  public List<ServiceAccountKey> listKeys(String serviceAccountEmail) throws IOException {

    List<ServiceAccountKey> keys =
        service
            .projects()
            .serviceAccounts()
            .keys()
            .list("projects/-/serviceAccounts/" + serviceAccountEmail)
            .execute()
            .getKeys();

    for (ServiceAccountKey key : keys) {
      System.out.println("Key: " + key.getName());
    }
    return keys;
  }
  // [END iam_list_keys]

  // [START iam_delete_key]
  public void deleteKey(String fullKeyName) throws IOException {

    service.projects().serviceAccounts().keys().delete(fullKeyName).execute();

    System.out.println("Deleted key: " + fullKeyName);
  }
  // [END iam_delete_key]
}
