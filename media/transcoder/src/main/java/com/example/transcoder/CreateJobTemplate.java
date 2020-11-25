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

// [START transcoder_create_job_template]

import com.google.cloud.video.transcoder.v1beta1.AudioStream;
import com.google.cloud.video.transcoder.v1beta1.CreateJobTemplateRequest;
import com.google.cloud.video.transcoder.v1beta1.ElementaryStream;
import com.google.cloud.video.transcoder.v1beta1.JobConfig;
import com.google.cloud.video.transcoder.v1beta1.JobTemplate;
import com.google.cloud.video.transcoder.v1beta1.LocationName;
import com.google.cloud.video.transcoder.v1beta1.MuxStream;
import com.google.cloud.video.transcoder.v1beta1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1beta1.VideoStream;
import java.io.IOException;

public class CreateJobTemplate {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String templateId = "my-job-template";

    createJobTemplate(projectId, location, templateId);
  }

  // Creates a job template.
  public static void createJobTemplate(String projectId, String location, String templateId)
      throws IOException {
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

      var createJobTemplateRequest =
          CreateJobTemplateRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setJobTemplateId(templateId)
              .setJobTemplate(JobTemplate.newBuilder().setConfig(config).build())
              .build();

      // Send the job template creation request and process the response.
      JobTemplate jobTemplate = transcoderServiceClient.createJobTemplate(createJobTemplateRequest);
      System.out.println("Job template: " + jobTemplate.getName());
    }
  }
}
// [END transcoder_create_job_template]
