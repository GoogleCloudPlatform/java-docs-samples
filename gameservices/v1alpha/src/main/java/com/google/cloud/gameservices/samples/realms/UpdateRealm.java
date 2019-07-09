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

// [START cloud_game_servers_realm_update]

import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.gaming.v1alpha.Realm;
import com.google.cloud.gaming.v1alpha.RealmsServiceClient;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateRealm {
  public static void updateRealm(String projectId, String regionId, String realmId)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // String projectId = "your-project-id";
    // String regionId = "us-central1-f";
    // String realmId = "your-realm-id";
    try (RealmsServiceClient client = RealmsServiceClient.create()) {
      String realmName = String.format(
          "projects/%s/locations/%s/realms/%s", projectId, regionId, realmId);

      Realm realm = Realm
          .newBuilder()
          .setTimeZone("America/New_York")
          .build();

      RetryingFuture<OperationSnapshot> poll = client.updateRealmAsync(
          realm, FieldMask.newBuilder().addPaths("time_zone").build())
          .getPollingFuture();

      if (poll.get(1, TimeUnit.MINUTES).isDone()) {
        Realm updatedPolicy = client.getRealm(realmName);
        System.out.println("Realm updated: " + updatedPolicy.getName());
      } else {
        throw new RuntimeException("Realm update request unsuccessful.");
      }
    }
  }
}
// [END cloud_game_servers_realm_update]
