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

// [START videostitcher_get_live_session]

import com.google.cloud.video.stitcher.v1.GetLiveSessionRequest;
import com.google.cloud.video.stitcher.v1.LiveSession;
import com.google.cloud.video.stitcher.v1.LiveSessionName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;

public class GetLiveSession {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String sessionId = "my-session-id";

    getLiveSession(projectId, location, sessionId);
  }

  // Gets a live session.
  public static LiveSession getLiveSession(String projectId, String location, String sessionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      GetLiveSessionRequest getLiveSessionRequest =
          GetLiveSessionRequest.newBuilder()
              .setName(LiveSessionName.of(projectId, location, sessionId).toString())
              .build();

      LiveSession response = videoStitcherServiceClient.getLiveSession(getLiveSessionRequest);
      System.out.println("Live session: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_get_live_session]
