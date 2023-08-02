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

// [START transcoder_create_job_with_static_overlay]

import com.google.cloud.video.transcoder.v1.AudioStream;
import com.google.cloud.video.transcoder.v1.CreateJobRequest;
import com.google.cloud.video.transcoder.v1.ElementaryStream;
import com.google.cloud.video.transcoder.v1.Input;
import com.google.cloud.video.transcoder.v1.Job;
import com.google.cloud.video.transcoder.v1.JobConfig;
import com.google.cloud.video.transcoder.v1.LocationName;
import com.google.cloud.video.transcoder.v1.MuxStream;
import com.google.cloud.video.transcoder.v1.Output;
import com.google.cloud.video.transcoder.v1.Overlay;
import com.google.cloud.video.transcoder.v1.Overlay.AnimationEnd;
import com.google.cloud.video.transcoder.v1.Overlay.AnimationStatic;
import com.google.cloud.video.transcoder.v1.Overlay.NormalizedCoordinate;
import com.google.cloud.video.transcoder.v1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1.VideoStream;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateJobWithStaticOverlay {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputUri = "gs://my-bucket/my-video-file";
    String overlayImageUri = "gs://my-bucket/my-overlay-image.jpg";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createJobWithStaticOverlay(projectId, location, inputUri, overlayImageUri, outputUri);
  }

  // Creates a job from an ad-hoc configuration and adds a static overlay to it.
  public static void createJobWithStaticOverlay(
      String projectId, String location, String inputUri, String overlayImageUri, String outputUri)
      throws IOException {
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

      // Create the overlay image. Image resolution is based on output video resolution.
      // To respect the original image aspect ratio, set either x or y to 0.0. This example
      // stretches the overlay image the full width and half of the height of the
      // output video.
      Overlay.Image overlayImage =
          Overlay.Image.newBuilder()
              .setUri(overlayImageUri)
              .setResolution(NormalizedCoordinate.newBuilder().setX(1).setY(0.5).build())
              .setAlpha(1)
              .build();

      // Create the starting animation (when the overlay appears). Use the values x: 0 and y: 0 to
      // position the top-left corner of the overlay in the top-left corner of the output video.
      Overlay.Animation animationStart =
          Overlay.Animation.newBuilder()
              .setAnimationStatic(
                  AnimationStatic.newBuilder()
                      .setXy(NormalizedCoordinate.newBuilder().setX(0).setY(0).build())
                      .setStartTimeOffset(Duration.newBuilder().setSeconds(0).build())
                      .build())
              .build();

      // Create the ending animation (when the overlay disappears). In this example, the overlay
      // disappears at the 10-second mark in the output video.
      Overlay.Animation animationEnd =
          Overlay.Animation.newBuilder()
              .setAnimationEnd(
                  AnimationEnd.newBuilder()
                      .setStartTimeOffset(Duration.newBuilder().setSeconds(10).build())
                      .build())
              .build();

      // Create the overlay and add the image and animations to it.
      Overlay overlay =
          Overlay.newBuilder()
              .setImage(overlayImage)
              .addAnimations(animationStart)
              .addAnimations(animationEnd)
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
              .addOverlays(overlay) // Add the overlay to the job config
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
// [END transcoder_create_job_with_static_overlay]
