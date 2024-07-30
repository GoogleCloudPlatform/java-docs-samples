/*
 * Copyright 2024 Google LLC
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

package com.example.stitcher;

// [START videostitcher_get_vod_config]

import com.google.cloud.video.stitcher.v1.GetVodConfigRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodConfig;
import com.google.cloud.video.stitcher.v1.VodConfigName;
import java.io.IOException;

public class GetVodConfig {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String vodConfigId = "my-vod-config-id";

    getVodConfig(projectId, location, vodConfigId);
  }

  // Gets a video on demand (VOD) config.
  public static VodConfig getVodConfig(String projectId, String location, String vodConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      GetVodConfigRequest getVodConfigRequest =
          GetVodConfigRequest.newBuilder()
              .setName(VodConfigName.of(projectId, location, vodConfigId).toString())
              .build();

      VodConfig response = videoStitcherServiceClient.getVodConfig(getVodConfigRequest);
      System.out.println("VOD config: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_get_vod_config]
