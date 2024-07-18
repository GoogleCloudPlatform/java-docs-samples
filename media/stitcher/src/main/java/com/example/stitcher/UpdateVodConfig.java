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

// [START videostitcher_update_vod_config]

import com.google.cloud.video.stitcher.v1.UpdateVodConfigRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodConfig;
import com.google.cloud.video.stitcher.v1.VodConfigName;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateVodConfig {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String vodConfigId = "my-vod-config-id";
    // Updated URI of the VOD stream to stitch; this URI must reference either an MPEG-DASH
    // manifest (.mpd) file or an M3U playlist manifest (.m3u8) file.
    String sourceUri = "https://storage.googleapis.com/my-bucket/main.mpd";

    updateVodConfig(projectId, location, vodConfigId, sourceUri);
  }

  // Updates the source URI in a video on demand (VOD) config.
  public static VodConfig updateVodConfig(
      String projectId, String location, String vodConfigId, String sourceUri)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      UpdateVodConfigRequest updateVodConfigRequest =
          UpdateVodConfigRequest.newBuilder()
              .setVodConfig(
                  VodConfig.newBuilder()
                      .setName(VodConfigName.of(projectId, location, vodConfigId).toString())
                      .setSourceUri(sourceUri)
                      .build())
              // Set the update mask to the sourceUri field in the existing VOD config. You must set
              // the mask to the field you want to update.
              .setUpdateMask(FieldMask.newBuilder().addPaths("sourceUri").build())
              .build();

      VodConfig response =
          videoStitcherServiceClient
              .updateVodConfigAsync(updateVodConfigRequest)
              .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Updated VOD config: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_update_vod_config]
