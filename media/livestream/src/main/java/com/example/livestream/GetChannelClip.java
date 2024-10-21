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

package com.example.livestream;

// [START livestream_get_channel_clip]

import com.google.cloud.video.livestream.v1.Clip;
import com.google.cloud.video.livestream.v1.ClipName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;

public class GetChannelClip {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String clipId = "my-channel-clip-id";

    getChannelClip(projectId, location, channelId, clipId);
  }

  public static Clip getChannelClip(
      String projectId, String location, String channelId, String clipId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      ClipName name = ClipName.of(projectId, location, channelId, clipId);
      Clip response = livestreamServiceClient.getClip(name);
      System.out.println("Channel clip: " + response.getName());
      return response;
    }
  }
}
// [END livestream_get_channel_clip]
