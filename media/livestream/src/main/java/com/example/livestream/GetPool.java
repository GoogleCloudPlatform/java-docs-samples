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

// [START livestream_get_pool]

import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.Pool;
import com.google.cloud.video.livestream.v1.PoolName;
import java.io.IOException;

public class GetPool {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String poolId = "default"; // only 1 pool supported per location

    getPool(projectId, location, poolId);
  }

  public static void getPool(String projectId, String location, String poolId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. In this example, try-with-resources is used
    // which automatically calls close() on the client to clean up resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      PoolName name = PoolName.of(projectId, location, poolId);
      Pool response = livestreamServiceClient.getPool(name);
      System.out.println("Pool: " + response.getName());
    }
  }
}
// [END livestream_get_pool]
