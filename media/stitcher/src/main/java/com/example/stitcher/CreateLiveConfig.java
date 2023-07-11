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

// [START videostitcher_create_live_config]

import com.google.cloud.video.stitcher.v1.AdTracking;
import com.google.cloud.video.stitcher.v1.CreateLiveConfigRequest;
import com.google.cloud.video.stitcher.v1.LiveConfig;
import com.google.cloud.video.stitcher.v1.LiveConfig.StitchingPolicy;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.SlateName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateLiveConfig {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String liveConfigId = "my-live-config-id";
    // Uri of the live stream to stitch; this URI must reference either an MPEG-DASH
    // manifest (.mpd) file or an M3U playlist manifest (.m3u8) file.
    String sourceUri = "https://storage.googleapis.com/my-bucket/main.mpd";
    // See Single Inline Linear
    // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
    String adTagUri = "https://pubads.g.doubleclick.net/gampad/ads...";
    String slateId = "my-slate-id";

    createLiveConfig(projectId, location, liveConfigId, sourceUri, adTagUri, slateId);
  }

  public static void createLiveConfig(String projectId, String location, String liveConfigId,
      String sourceUri, String adTagUri, String slateId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CreateLiveConfigRequest createLiveConfigRequest =
          CreateLiveConfigRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setLiveConfigId(liveConfigId)
              .setLiveConfig(LiveConfig.newBuilder()
                  .setSourceUri(sourceUri)
                  .setAdTagUri(adTagUri)
                  .setDefaultSlate(SlateName.format(projectId, location, slateId))
                  .setAdTracking(AdTracking.SERVER)
                  .setStitchingPolicy(StitchingPolicy.CUT_CURRENT).build())
              .build();

      LiveConfig response = videoStitcherServiceClient.createLiveConfigAsync(
              createLiveConfigRequest)
          .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Created new live config: " + response.getName());
    }
  }
}
// [END videostitcher_create_live_config]
