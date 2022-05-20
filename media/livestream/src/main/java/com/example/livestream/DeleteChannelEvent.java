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

// [START livestream_delete_channel_event]

import com.google.cloud.video.livestream.v1.DeleteEventRequest;
import com.google.cloud.video.livestream.v1.EventName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;

public class DeleteChannelEvent {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String eventId = "my-channel-event-id";

    deleteChannelEvent(projectId, location, channelId, eventId);
  }

  public static void deleteChannelEvent(
      String projectId, String location, String channelId, String eventId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var deleteEventRequest =
          DeleteEventRequest.newBuilder()
              .setName(EventName.of(projectId, location, channelId, eventId).toString())
              .build();

      livestreamServiceClient.deleteEvent(deleteEventRequest);
      System.out.println("Deleted channel event");
    }
  }
}
// [END livestream_delete_channel_event]
