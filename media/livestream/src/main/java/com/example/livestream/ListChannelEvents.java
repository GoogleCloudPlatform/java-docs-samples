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

package com.example.livestream;

// [START livestream_list_channel_events]

import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.Event;
import com.google.cloud.video.livestream.v1.ListEventsRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;

public class ListChannelEvents {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";

    listChannelEvents(projectId, location, channelId);
  }

  public static void listChannelEvents(String projectId, String location, String channelId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var listEventsRequest =
          ListEventsRequest.newBuilder()
              .setParent(ChannelName.of(projectId, location, channelId).toString())
              .build();

      LivestreamServiceClient.ListEventsPagedResponse response =
          livestreamServiceClient.listEvents(listEventsRequest);
      System.out.println("Channel events:");

      for (Event event : response.iterateAll()) {
        System.out.println(event.getName());
      }
    }
  }
}
// [END livestream_list_channel_events]
