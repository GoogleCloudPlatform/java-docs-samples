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

// [START videostitcher_create_vod_session]

import com.google.cloud.video.stitcher.v1.AdTracking;
import com.google.cloud.video.stitcher.v1.CreateVodSessionRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodConfigName;
import com.google.cloud.video.stitcher.v1.VodSession;
import java.io.IOException;

public class CreateVodSession {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String vodConfigId = "my-vod-config-id";

    createVodSession(projectId, location, vodConfigId);
  }

  // Creates a video on demand (VOD) session using the parameters in the designated VOD config.
  // For more information, see
  // https://cloud.google.com/video-stitcher/docs/how-to/creating-vod-sessions.
  public static VodSession createVodSession(String projectId, String location, String vodConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CreateVodSessionRequest createVodSessionRequest =
          CreateVodSessionRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setVodSession(
                  VodSession.newBuilder()
                      .setVodConfig(VodConfigName.format(projectId, location, vodConfigId))
                      .setAdTracking(AdTracking.SERVER)
                      .build())
              .build();

      VodSession response = videoStitcherServiceClient.createVodSession(createVodSessionRequest);
      System.out.println("Created VOD session: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_create_vod_session]
