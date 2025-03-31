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

// [START videostitcher_create_live_session]

import com.google.cloud.video.stitcher.v1.CreateLiveSessionRequest;
import com.google.cloud.video.stitcher.v1.LiveConfigName;
import com.google.cloud.video.stitcher.v1.LiveSession;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;

public class CreateLiveSession {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String liveConfigId = "my-live-config-id";

    createLiveSession(projectId, location, liveConfigId);
  }

  // Creates a live session given the parameters in the supplied live config.
  // For more information, see
  // https://cloud.google.com/video-stitcher/docs/how-to/managing-live-sessions.
  public static LiveSession createLiveSession(
      String projectId, String location, String liveConfigId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CreateLiveSessionRequest createLiveSessionRequest =
          CreateLiveSessionRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setLiveSession(
                  LiveSession.newBuilder()
                      .setLiveConfig(LiveConfigName.format(projectId, location, liveConfigId)))
              .build();

      LiveSession response = videoStitcherServiceClient.createLiveSession(createLiveSessionRequest);
      System.out.println("Created live session: " + response.getName());
      System.out.println("Play URI: " + response.getPlayUri());
      return response;
    }
  }
}
// [END videostitcher_create_live_session]
