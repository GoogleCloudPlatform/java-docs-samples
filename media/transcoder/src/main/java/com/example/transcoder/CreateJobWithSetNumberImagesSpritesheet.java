/*
 * Copyright 2021 Google LLC
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

package com.example.transcoder;

// [START transcoder_create_job_with_set_number_images_spritesheet]

import com.google.cloud.video.transcoder.v1.AudioStream;
import com.google.cloud.video.transcoder.v1.CreateJobRequest;
import com.google.cloud.video.transcoder.v1.ElementaryStream;
import com.google.cloud.video.transcoder.v1.Input;
import com.google.cloud.video.transcoder.v1.Job;
import com.google.cloud.video.transcoder.v1.JobConfig;
import com.google.cloud.video.transcoder.v1.LocationName;
import com.google.cloud.video.transcoder.v1.MuxStream;
import com.google.cloud.video.transcoder.v1.Output;
import com.google.cloud.video.transcoder.v1.SpriteSheet;
import com.google.cloud.video.transcoder.v1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1.VideoStream;
import java.io.IOException;

public class CreateJobWithSetNumberImagesSpritesheet {

  public static final String smallSpritesheetFilePrefix = "small-sprite-sheet";
  public static final String largeSpritesheetFilePrefix = "large-sprite-sheet";
  public static final String spritesheetFileSuffix = "0000000000.jpeg";

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputUri = "gs://my-bucket/my-video-file";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createJobWithSetNumberImagesSpritesheet(projectId, location, inputUri, outputUri);
  }

  // Creates a job from an ad-hoc configuration and generates two spritesheets from the input video.
  // Each spritesheet contains a set number of images.
  public static void createJobWithSetNumberImagesSpritesheet(
      String projectId, String location, String inputUri, String outputUri) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TranscoderServiceClient transcoderServiceClient = TranscoderServiceClient.create()) {

      VideoStream videoStream0 =
          VideoStream.newBuilder()
              .setH264(
                  VideoStream.H264CodecSettings.newBuilder()
                      .setBitrateBps(550000)
                      .setFrameRate(60)
                      .setHeightPixels(360)
                      .setWidthPixels(640))
              .build();

      AudioStream audioStream0 =
          AudioStream.newBuilder().setCodec("aac").setBitrateBps(64000).build();

      // Generates a 10x10 spritesheet of small images from the input video. To preserve the source
      // aspect ratio, you should set the spriteWidthPixels field or the spriteHeightPixels
      // field, but not both.
      SpriteSheet smallSpriteSheet =
          SpriteSheet.newBuilder()
              .setFilePrefix(smallSpritesheetFilePrefix)
              .setSpriteHeightPixels(32)
              .setSpriteWidthPixels(64)
              .setColumnCount(10)
              .setRowCount(10)
              .setTotalCount(100)
              .build();

      // Generates a 10x10 spritesheet of larger images from the input video.
      SpriteSheet largeSpriteSheet =
          SpriteSheet.newBuilder()
              .setFilePrefix(largeSpritesheetFilePrefix)
              .setSpriteHeightPixels(72)
              .setSpriteWidthPixels(128)
              .setColumnCount(10)
              .setRowCount(10)
              .setTotalCount(100)
              .build();

      JobConfig config =
          JobConfig.newBuilder()
              .addInputs(Input.newBuilder().setKey("input0").setUri(inputUri))
              .setOutput(Output.newBuilder().setUri(outputUri))
              .addElementaryStreams(
                  ElementaryStream.newBuilder()
                      .setKey("video_stream0")
                      .setVideoStream(videoStream0))
              .addElementaryStreams(
                  ElementaryStream.newBuilder()
                      .setKey("audio_stream0")
                      .setAudioStream(audioStream0))
              .addMuxStreams(
                  MuxStream.newBuilder()
                      .setKey("sd")
                      .setContainer("mp4")
                      .addElementaryStreams("video_stream0")
                      .addElementaryStreams("audio_stream0")
                      .build())
              .addSpriteSheets(smallSpriteSheet) // Add the spritesheet config to the job config
              .addSpriteSheets(largeSpriteSheet) // Add the spritesheet config to the job config
              .build();

      CreateJobRequest createJobRequest =
          CreateJobRequest.newBuilder()
              .setJob(
                  Job.newBuilder()
                      .setInputUri(inputUri)
                      .setOutputUri(outputUri)
                      .setConfig(config)
                      .build())
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      // Send the job creation request and process the response.
      Job job = transcoderServiceClient.createJob(createJobRequest);
      System.out.println("Job: " + job.getName());
    }
  }
}
// [END transcoder_create_job_with_set_number_images_spritesheet]
