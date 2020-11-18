/*
 * Copyright 2020 Google LLC
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

// [START transcoder_create_job_from_ad_hoc]

import com.google.cloud.video.transcoder.v1beta1.AudioStream;
import com.google.cloud.video.transcoder.v1beta1.CreateJobRequest;
import com.google.cloud.video.transcoder.v1beta1.ElementaryStream;
import com.google.cloud.video.transcoder.v1beta1.Input;
import com.google.cloud.video.transcoder.v1beta1.Job;
import com.google.cloud.video.transcoder.v1beta1.JobConfig;
import com.google.cloud.video.transcoder.v1beta1.LocationName;
import com.google.cloud.video.transcoder.v1beta1.MuxStream;
import com.google.cloud.video.transcoder.v1beta1.Output;
import com.google.cloud.video.transcoder.v1beta1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1beta1.VideoStream;
import java.io.IOException;

public class CreateJobFromAdHoc {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputUri = "gs://my-bucket/my-video-file";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createJobFromAdHoc(projectId, location, inputUri, outputUri);
  }

  // Creates a job from an ad-hoc configuration.
  public static void createJobFromAdHoc(
      String projectId, String location, String inputUri, String outputUri) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TranscoderServiceClient transcoderServiceClient = TranscoderServiceClient.create()) {

      VideoStream videoStream0 =
          VideoStream.newBuilder()
              .setCodec("h264")
              .setBitrateBps(550000)
              .setFrameRate(60)
              .setHeightPixels(360)
              .setWidthPixels(640)
              .build();
      VideoStream videoStream1 =
          VideoStream.newBuilder()
              .setCodec("h264")
              .setBitrateBps(2500000)
              .setFrameRate(60)
              .setHeightPixels(720)
              .setWidthPixels(1280)
              .build();
      AudioStream audioStream0 =
          AudioStream.newBuilder().setCodec("aac").setBitrateBps(64000).build();
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
                      .setKey("video_stream1")
                      .setVideoStream(videoStream1))
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
              .addMuxStreams(
                  MuxStream.newBuilder()
                      .setKey("hd")
                      .setContainer("mp4")
                      .addElementaryStreams("video_stream1")
                      .addElementaryStreams("audio_stream0")
                      .build())
              .build();

      var createJobRequest =
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
// [END transcoder_create_job_from_ad_hoc]
