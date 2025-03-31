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

package com.example.stitcher;

// [START videostitcher_get_live_config]

import com.google.cloud.video.stitcher.v1.GetLiveConfigRequest;
import com.google.cloud.video.stitcher.v1.LiveConfig;
import com.google.cloud.video.stitcher.v1.LiveConfigName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;

public class GetLiveConfig {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String liveConfigId = "my-live-config-id";

    getLiveConfig(projectId, location, liveConfigId);
  }

  // Gets a live config.
  public static LiveConfig getLiveConfig(String projectId, String location, String liveConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      GetLiveConfigRequest getLiveConfigRequest =
          GetLiveConfigRequest.newBuilder()
              .setName(LiveConfigName.of(projectId, location, liveConfigId).toString())
              .build();

      LiveConfig response = videoStitcherServiceClient.getLiveConfig(getLiveConfigRequest);
      System.out.println("Live config: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_get_live_config]
