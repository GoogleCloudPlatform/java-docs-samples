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

// [START videostitcher_delete_vod_config]

import com.google.cloud.video.stitcher.v1.DeleteVodConfigRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodConfigName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteVodConfig {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String vodConfigId = "my-vod-config-id";

    deleteVodConfig(projectId, location, vodConfigId);
  }

  // Deletes a video on demand (VOD) config.
  public static void deleteVodConfig(String projectId, String location, String vodConfigId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      DeleteVodConfigRequest deleteVodConfigRequest =
          DeleteVodConfigRequest.newBuilder()
              .setName(VodConfigName.of(projectId, location, vodConfigId).toString())
              .build();

      videoStitcherServiceClient
          .deleteVodConfigAsync(deleteVodConfigRequest)
          .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Deleted VOD config");
    }
  }
}
// [END videostitcher_delete_vod_config]
