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

package com.example.transcoder;

// [START transcoder_create_job_with_standalone_captions]

import com.google.cloud.video.transcoder.v1.AudioStream;
import com.google.cloud.video.transcoder.v1.CreateJobRequest;
import com.google.cloud.video.transcoder.v1.EditAtom;
import com.google.cloud.video.transcoder.v1.ElementaryStream;
import com.google.cloud.video.transcoder.v1.Input;
import com.google.cloud.video.transcoder.v1.Job;
import com.google.cloud.video.transcoder.v1.JobConfig;
import com.google.cloud.video.transcoder.v1.LocationName;
import com.google.cloud.video.transcoder.v1.Manifest;
import com.google.cloud.video.transcoder.v1.Manifest.ManifestType;
import com.google.cloud.video.transcoder.v1.MuxStream;
import com.google.cloud.video.transcoder.v1.Output;
import com.google.cloud.video.transcoder.v1.SegmentSettings;
import com.google.cloud.video.transcoder.v1.TextStream;
import com.google.cloud.video.transcoder.v1.TextStream.TextMapping;
import com.google.cloud.video.transcoder.v1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1.VideoStream;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateJobWithStandaloneCaptions {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputVideoUri = "gs://my-bucket/my-video-file";
    String inputCaptionsUri = "gs://my-bucket/my-captions-file";
    String outputUri = "gs://my-bucket/my-output-folder/";

    createJobWithStandaloneCaptions(
        projectId, location, inputVideoUri, inputCaptionsUri, outputUri);
  }

  // Creates a job from an ad-hoc configuration that can use captions from a standalone file.
  public static void createJobWithStandaloneCaptions(
      String projectId,
      String location,
      String inputVideoUri,
      String inputCaptionsUri,
      String outputUri)
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

      TextStream textStream0 =
          TextStream.newBuilder()
              .setCodec("webvtt")
              .addMapping(
                  0,
                  TextMapping.newBuilder()
                      .setAtomKey("atom0")
                      .setInputKey("caption_input0")
                      .setInputTrack(0)
                      .build())
              .build();

      JobConfig config =
          JobConfig.newBuilder()
              .addInputs(Input.newBuilder().setKey("input0").setUri(inputVideoUri))
              .addInputs(Input.newBuilder().setKey("caption_input0").setUri(inputCaptionsUri))
              .addEditList(
                  0, // Index in the edit list
                  EditAtom.newBuilder()
                      .setKey("atom0")
                      .addInputs("input0")
                      .addInputs("caption_input0")
                      .build())
              .setOutput(Output.newBuilder().setUri(outputUri))
              .addElementaryStreams(
                  ElementaryStream.newBuilder()
                      .setKey("video_stream0")
                      .setVideoStream(videoStream0))
              .addElementaryStreams(
                  ElementaryStream.newBuilder()
                      .setKey("audio_stream0")
                      .setAudioStream(audioStream0))
              .addElementaryStreams(
                  ElementaryStream.newBuilder().setKey("vtt_stream0").setTextStream(textStream0))
              .addMuxStreams(
                  0,
                  MuxStream.newBuilder()
                      .setKey("sd_hls_fmp4")
                      .setContainer("fmp4")
                      .addElementaryStreams("video_stream0")
                      .build())
              .addMuxStreams(
                  1,
                  MuxStream.newBuilder()
                      .setKey("audio_hls_fmp4")
                      .setContainer("fmp4")
                      .addElementaryStreams("audio_stream0")
                      .build())
              .addMuxStreams(
                  2,
                  MuxStream.newBuilder()
                      .setKey("text_vtt")
                      .setContainer("vtt")
                      .addElementaryStreams("vtt_stream0")
                      .setSegmentSettings(
                          SegmentSettings.newBuilder()
                              .setSegmentDuration(Duration.newBuilder().setSeconds(6).build())
                              .setIndividualSegments(true)
                              .build())
                      .build())
              .addManifests(
                  0,
                  Manifest.newBuilder()
                      .setFileName("manifest.m3u8")
                      .setType(ManifestType.HLS)
                      .addMuxStreams("sd_hls_fmp4")
                      .addMuxStreams("audio_hls_fmp4")
                      .addMuxStreams("text_vtt")
                      .build())
              .build();

      CreateJobRequest createJobRequest =
          CreateJobRequest.newBuilder()
              .setJob(Job.newBuilder().setOutputUri(outputUri).setConfig(config).build())
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      // Send the job creation request and process the response.
      Job job = transcoderServiceClient.createJob(createJobRequest);
      System.out.println("Job: " + job.getName());
    }
  }
}
// [END transcoder_create_job_with_standalone_captions]
