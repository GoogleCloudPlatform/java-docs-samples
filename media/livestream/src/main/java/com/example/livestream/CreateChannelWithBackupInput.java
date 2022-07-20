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

// [START livestream_create_channel_with_backup_input]

import com.google.cloud.video.livestream.v1.AudioStream;
import com.google.cloud.video.livestream.v1.Channel;
import com.google.cloud.video.livestream.v1.Channel.Output;
import com.google.cloud.video.livestream.v1.CreateChannelRequest;
import com.google.cloud.video.livestream.v1.ElementaryStream;
import com.google.cloud.video.livestream.v1.InputAttachment;
import com.google.cloud.video.livestream.v1.InputAttachment.AutomaticFailover;
import com.google.cloud.video.livestream.v1.InputName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LocationName;
import com.google.cloud.video.livestream.v1.Manifest;
import com.google.cloud.video.livestream.v1.Manifest.ManifestType;
import com.google.cloud.video.livestream.v1.MuxStream;
import com.google.cloud.video.livestream.v1.SegmentSettings;
import com.google.cloud.video.livestream.v1.VideoStream;
import com.google.cloud.video.livestream.v1.VideoStream.H264CodecSettings;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateChannelWithBackupInput {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String primaryInputId = "my-primary-input-id";
    String backupInputId = "my-backup-input-id";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createChannelWithBackupInput(
        projectId, location, channelId, primaryInputId, backupInputId, outputUri);
  }

  public static void createChannelWithBackupInput(
      String projectId,
      String location,
      String channelId,
      String primaryInputId,
      String backupInputId,
      String outputUri)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      VideoStream videoStream =
          VideoStream.newBuilder()
              .setH264(
                  H264CodecSettings.newBuilder()
                      .setProfile("high")
                      .setBitrateBps(3000000)
                      .setFrameRate(30)
                      .setHeightPixels(720)
                      .setWidthPixels(1280))
              .build();

      AudioStream audioStream =
          AudioStream.newBuilder().setCodec("aac").setChannelCount(2).setBitrateBps(160000).build();

      var createChannelRequest =
          CreateChannelRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setChannelId(channelId)
              .setChannel(
                  Channel.newBuilder()
                      .addInputAttachments(
                          0,
                          InputAttachment.newBuilder()
                              .setKey("my-primary-input")
                              .setInput(
                                  InputName.of(projectId, location, primaryInputId).toString())
                              .setAutomaticFailover(
                                  AutomaticFailover.newBuilder()
                                      .addInputKeys("my-backup-input")
                                      .build())
                              .build())
                      .addInputAttachments(
                          1,
                          InputAttachment.newBuilder()
                              .setKey("my-backup-input")
                              .setInput(
                                  InputName.of(projectId, location, backupInputId).toString()))
                      .setOutput(Output.newBuilder().setUri(outputUri).build())
                      .addElementaryStreams(
                          ElementaryStream.newBuilder()
                              .setKey("es_video")
                              .setVideoStream(videoStream))
                      .addElementaryStreams(
                          ElementaryStream.newBuilder()
                              .setKey("es_audio")
                              .setAudioStream(audioStream))
                      .addMuxStreams(
                          MuxStream.newBuilder()
                              .setKey("mux_video")
                              .addElementaryStreams("es_video")
                              .setSegmentSettings(
                                  SegmentSettings.newBuilder()
                                      .setSegmentDuration(
                                          Duration.newBuilder().setSeconds(2).build())
                                      .build())
                              .build())
                      .addMuxStreams(
                          MuxStream.newBuilder()
                              .setKey("mux_audio")
                              .addElementaryStreams("es_audio")
                              .setSegmentSettings(
                                  SegmentSettings.newBuilder()
                                      .setSegmentDuration(
                                          Duration.newBuilder().setSeconds(2).build())
                                      .build())
                              .build())
                      .addManifests(
                          Manifest.newBuilder()
                              .setFileName("manifest.m3u8")
                              .setType(ManifestType.HLS)
                              .addMuxStreams("mux_video")
                              .addMuxStreams("mux_audio")
                              .setMaxSegmentCount(5)
                              .build()))
              .build();

      Channel result =
          livestreamServiceClient.createChannelAsync(createChannelRequest).get(10, TimeUnit.MINUTES);
      System.out.println("Channel: " + result.getName());
    }
  }
}
// [END livestream_create_channel_with_backup_input]
