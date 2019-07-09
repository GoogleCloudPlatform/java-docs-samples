/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.gameservices.samples.realms;

// [START cloud_game_servers_realm_create]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.CreateRealmRequest;
import com.google.cloud.gaming.v1alpha.Realm;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateRealm {
  public static void createRealm(String projectId, String regionId, String realmId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1-f";
    // String realmId = "your-realm-id";
    try (RealmsServiceClient client = RealmsServiceClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, regionId);
      String realmName = String.format("%s/realms/%s", parent, realmId);

      Realm realm = Realm
          .newBuilder()
          .setName(realmName)
          .setTimeZone("America/Los_Angeles")
          .build();

      RetryingFuture<OperationSnapshot> poll = client.createRealmAsync(
          CreateRealmRequest
              .newBuilder()
              .setParent(parent)
              .setRealmId(realmId)
              .setRealm(realm)
              .build()).getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        System.out.println("Realm created: " + realm.getName());
      } else {
        throw new RuntimeException("Realm create request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_realm_create]
