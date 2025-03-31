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

// [START videostitcher_delete_live_config]

import com.google.cloud.video.stitcher.v1.DeleteLiveConfigRequest;
import com.google.cloud.video.stitcher.v1.LiveConfigName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteLiveConfig {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String liveConfigId = "my-live-config-id";

    deleteLiveConfig(projectId, location, liveConfigId);
  }

  // Deletes a live config.
  public static void deleteLiveConfig(String projectId, String location, String liveConfigId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      DeleteLiveConfigRequest deleteLiveConfigRequest =
          DeleteLiveConfigRequest.newBuilder()
              .setName(LiveConfigName.of(projectId, location, liveConfigId).toString())
              .build();

      videoStitcherServiceClient
          .deleteLiveConfigAsync(deleteLiveConfigRequest)
          .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Deleted live config");
    }
  }
}
// [END videostitcher_delete_live_config]
