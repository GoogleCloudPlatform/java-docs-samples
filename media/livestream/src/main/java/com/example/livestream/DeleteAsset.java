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

// [START livestream_delete_asset]

import com.google.cloud.video.livestream.v1.AssetName;
import com.google.cloud.video.livestream.v1.DeleteAssetRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteAsset {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String assetId = "my-asset-id";

    deleteAsset(projectId, location, assetId);
  }

  public static void deleteAsset(String projectId, String location, String assetId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create();
    var deleteAssetRequest =
        DeleteAssetRequest.newBuilder()
            .setName(AssetName.of(projectId, location, assetId).toString())
            .build();
    // First API call in a project can take up to 10 minutes.
    livestreamServiceClient.deleteAssetAsync(deleteAssetRequest).get(10, TimeUnit.MINUTES);
    System.out.println("Deleted asset");
    livestreamServiceClient.close();
  }
}
// [END livestream_delete_asset]
