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

// [START livestream_get_asset]

import com.google.cloud.video.livestream.v1.Asset;
import com.google.cloud.video.livestream.v1.AssetName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;

public class GetAsset {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String assetId = "my-asset-id";

    getAsset(projectId, location, assetId);
  }

  public static void getAsset(String projectId, String location, String assetId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. In this example, try-with-resources is used
    // which automatically calls close() on the client to clean up resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      AssetName name = AssetName.of(projectId, location, assetId);
      Asset response = livestreamServiceClient.getAsset(name);
      System.out.println("Asset: " + response.getName());
    }
  }
}
// [END livestream_get_asset]
