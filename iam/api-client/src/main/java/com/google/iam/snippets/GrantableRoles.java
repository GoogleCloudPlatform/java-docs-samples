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
import com.google.api.services.iam.v1.model.QueryGrantableRolesRequest;
import com.google.api.services.iam.v1.model.QueryGrantableRolesResponse;
import com.google.api.services.iam.v1.model.Role;
import java.util.Collections;

public class GrantableRoles {

  public static void main(String[] args) throws Exception {

    GoogleCredential credential =
        GoogleCredential.getApplicationDefault()
            .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

    Iam service =
        new Iam.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("grantable-roles")
            .build();

    String fullResourceName = args[0];

    // [START iam_view_grantable_roles]
    QueryGrantableRolesRequest request = new QueryGrantableRolesRequest();
    request.setFullResourceName(fullResourceName);

    QueryGrantableRolesResponse response = service.roles().queryGrantableRoles(request).execute();

    for (Role role : response.getRoles()) {
      System.out.println("Title: " + role.getTitle());
      System.out.println("Name: " + role.getName());
      System.out.println("Description: " + role.getDescription());
      System.out.println();
    }
    // [END iam_view_grantable_roles]
  }
}
