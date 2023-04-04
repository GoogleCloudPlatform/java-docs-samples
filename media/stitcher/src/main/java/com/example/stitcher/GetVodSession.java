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

// [START videostitcher_get_vod_session]

import com.google.cloud.video.stitcher.v1.GetVodSessionRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodSession;
import com.google.cloud.video.stitcher.v1.VodSessionName;
import java.io.IOException;

public class GetVodSession {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String sessionId = "my-session-id";

    getVodSession(projectId, location, sessionId);
  }

  public static void getVodSession(String projectId, String location, String sessionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      GetVodSessionRequest getVodSessionRequest =
          GetVodSessionRequest.newBuilder()
              .setName(VodSessionName.of(projectId, location, sessionId).toString())
              .build();

      VodSession response = videoStitcherServiceClient.getVodSession(getVodSessionRequest);
      System.out.println("VOD session: " + response.getName());
    }
  }
}
// [END videostitcher_get_vod_session]
