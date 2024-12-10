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

// [START livestream_create_channel_clip]

import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.Clip;
import com.google.cloud.video.livestream.v1.Clip.ClipManifest;
import com.google.cloud.video.livestream.v1.Clip.Slice;
import com.google.cloud.video.livestream.v1.Clip.TimeSlice;
import com.google.cloud.video.livestream.v1.CreateClipRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateChannelClip {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String clipId = "my-channel-clip-id";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createChannelClip(projectId, location, channelId, clipId, outputUri);
  }

  // Creates a channel clip. A channel clip is a sub-resource of a channel. You can use a channel
  // clip to create video on demand (VOD) files from a live stream. These VOD files are saved to
  // Cloud Storage.
  public static Clip createChannelClip(
      String projectId, String location, String channelId, String clipId, String outputUri)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.

    // Create a 20-second clip starting 40 seconds ago
    Instant recent = Instant.now().minusSeconds(20); // 20 seconds ago
    Instant earlier = Instant.now().minusSeconds(40); // 40 seconds ago

    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      CreateClipRequest createClipRequest =
          CreateClipRequest.newBuilder()
              .setParent(ChannelName.of(projectId, location, channelId).toString())
              .setClipId(clipId)
              .setClip(
                  Clip.newBuilder()
                      .setOutputUri(outputUri)
                      .addSlices(
                          Slice.newBuilder()
                              .setTimeSlice(
                                  TimeSlice.newBuilder()
                                      .setMarkinTime(
                                          Timestamp.newBuilder()
                                              .setSeconds(earlier.getEpochSecond())
                                              .setNanos(earlier.getNano()))
                                      .setMarkoutTime(
                                          Timestamp.newBuilder()
                                              .setSeconds(recent.getEpochSecond())
                                              .setNanos(recent.getNano()))
                                      .build())
                              .build())
                      .addClipManifests(
                          ClipManifest.newBuilder().setManifestKey("manifest_hls").build())
                      .build())
              .build();

      Clip response =
          livestreamServiceClient.createClipAsync(createClipRequest).get(10, TimeUnit.MINUTES);
      System.out.println("Channel clip: " + response.getName());
      return response;
    }
  }
}
// [END livestream_create_channel_clip]
