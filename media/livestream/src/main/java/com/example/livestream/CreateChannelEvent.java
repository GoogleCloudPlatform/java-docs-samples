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

// [START livestream_create_channel_event]

import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.CreateEventRequest;
import com.google.cloud.video.livestream.v1.Event;
import com.google.cloud.video.livestream.v1.Event.AdBreakTask;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateChannelEvent {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String eventId = "my-channel-event-id";

    createChannelEvent(projectId, location, channelId, eventId);
  }

  public static void createChannelEvent(
      String projectId, String location, String channelId, String eventId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var createEventRequest =
          CreateEventRequest.newBuilder()
              .setParent(ChannelName.of(projectId, location, channelId).toString())
              .setEventId(eventId)
              .setEvent(
                  Event.newBuilder()
                      .setAdBreak(
                          AdBreakTask.newBuilder()
                              .setDuration(Duration.newBuilder().setSeconds(30).build())
                              .build())
                      .setExecuteNow(true)
                      .build())
              .build();

      Event response = livestreamServiceClient.createEvent(createEventRequest);
      System.out.println("Channel event: " + response.getName());
    }
  }
}
// [END livestream_create_channel_event]
