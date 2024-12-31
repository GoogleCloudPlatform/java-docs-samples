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

// [START livestream_list_channel_clips]

import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.Clip;
import com.google.cloud.video.livestream.v1.ListClipsRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient.ListClipsPagedResponse;
import java.io.IOException;

public class ListChannelClips {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";

    listChannelClips(projectId, location, channelId);
  }

  public static ListClipsPagedResponse listChannelClips(
      String projectId, String location, String channelId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      ListClipsRequest listClipsRequest =
          ListClipsRequest.newBuilder()
              .setParent(ChannelName.of(projectId, location, channelId).toString())
              .build();

      LivestreamServiceClient.ListClipsPagedResponse response =
          livestreamServiceClient.listClips(listClipsRequest);
      System.out.println("Channel clips:");

      for (Clip clip : response.iterateAll()) {
        System.out.println(clip.getName());
      }
      return response;
    }
  }
}
// [END livestream_list_channel_clips]
