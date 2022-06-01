/*
 * Copyright 2022 Google LLC
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

// [START video_stitcher_create_vod_session]

import com.google.cloud.video.stitcher.v1.CreateVodSessionRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodSession;
import java.io.IOException;

public class CreateVodSession {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    // Uri of the media to stitch; this URI must reference either an MPEG-DASH
    // manifest (.mpd) file or an M3U playlist manifest (.m3u8) file.
    String sourceUri = "https://storage.googleapis.com/my-bucket/main.mpd";
    // See VMAP Pre-roll
    // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
    String adTagUri = "https://pubads.g.doubleclick.net/gampad/ads...";
    createVodSession(projectId, location, sourceUri, adTagUri);
  }

  public static void createVodSession(
      String projectId, String location, String sourceUri, String adTagUri) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CreateVodSessionRequest createVodSessionRequest =
          CreateVodSessionRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setVodSession(
                  VodSession.newBuilder().setSourceUri(sourceUri).setAdTagUri(adTagUri).build())
              .build();

      VodSession response = videoStitcherServiceClient.createVodSession(createVodSessionRequest);
      System.out.println("Created VOD session: " + response.getName());
    }
  }
}
// [END video_stitcher_create_vod_session]
