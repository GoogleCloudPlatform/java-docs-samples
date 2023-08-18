/*
 * Copyright 2023 Google LLC
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

package com.example.livestream;

// [START livestream_update_pool]

import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.Pool;
import com.google.cloud.video.livestream.v1.Pool.NetworkConfig;
import com.google.cloud.video.livestream.v1.PoolName;
import com.google.cloud.video.livestream.v1.UpdatePoolRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdatePool {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String poolId = "default";
    String peeredNetwork = "";

    updatePool(projectId, location, poolId, peeredNetwork);
  }

  public static void updatePool(String projectId, String location, String poolId,
      String peeredNetwork)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create();
    var updatePoolRequest =
        UpdatePoolRequest.newBuilder()
            .setPool(
                Pool.newBuilder()
                    .setName(PoolName.of(projectId, location, poolId).toString())
                    .setNetworkConfig(
                        NetworkConfig.newBuilder()
                            .setPeeredNetwork(peeredNetwork)
                            .build()

                    ))
            .setUpdateMask(FieldMask.newBuilder().addPaths("network_config").build())
            .build();
    // Update pool can take 20+ minutes.
    Pool result =
        livestreamServiceClient.updatePoolAsync(updatePoolRequest).get(20, TimeUnit.MINUTES);
    System.out.println("Updated pool: " + result.getName());
    livestreamServiceClient.close();
  }
}
// [END livestream_update_pool]
