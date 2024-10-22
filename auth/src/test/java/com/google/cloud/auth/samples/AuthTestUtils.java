/*
 * Copyright 2024 Google Inc.
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

package com.google.cloud.auth.samples;

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.ApiTarget;
import com.google.api.apikeys.v2.CreateKeyRequest;
import com.google.api.apikeys.v2.Key;
import com.google.api.apikeys.v2.LocationName;
import com.google.api.apikeys.v2.Restrictions;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AuthTestUtils {

  public static Key createTestApiKey(
      String projectId, String keyDisplayName, String service, String method)
      throws IllegalStateException {
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {
      Key key =
          Key.newBuilder()
              .setDisplayName(keyDisplayName)
              .setRestrictions(
                  Restrictions.newBuilder()
                      .addApiTargets(
                          ApiTarget.newBuilder().setService(service).addMethods(method).build())
                      .build())
              .build();

      CreateKeyRequest createKeyRequest =
          CreateKeyRequest.newBuilder()
              // API keys can only be global.
              .setParent(LocationName.of(projectId, "global").toString())
              .setKey(key)
              .build();
      return apiKeysClient.createKeyAsync(createKeyRequest).get(3, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new IllegalStateException("Error trying to create API Key " + e.getMessage());
    }
  }

  public static void deleteTestApiKey(String keyName) throws IOException {
    try (ApiKeysClient apiKeysClient = ApiKeysClient.create()) {
      apiKeysClient.deleteKeyAsync(keyName);
    }
  }
}
