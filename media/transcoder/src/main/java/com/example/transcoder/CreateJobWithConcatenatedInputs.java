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

// [START transcoder_create_job_with_concatenated_inputs]

import com.google.cloud.video.transcoder.v1.AudioStream;
import com.google.cloud.video.transcoder.v1.CreateJobRequest;
import com.google.cloud.video.transcoder.v1.EditAtom;
import com.google.cloud.video.transcoder.v1.ElementaryStream;
import com.google.cloud.video.transcoder.v1.Input;
import com.google.cloud.video.transcoder.v1.Job;
import com.google.cloud.video.transcoder.v1.JobConfig;
import com.google.cloud.video.transcoder.v1.LocationName;
import com.google.cloud.video.transcoder.v1.MuxStream;
import com.google.cloud.video.transcoder.v1.Output;
import com.google.cloud.video.transcoder.v1.TranscoderServiceClient;
import com.google.cloud.video.transcoder.v1.VideoStream;
import com.google.protobuf.Duration;
import java.io.IOException;

public class CreateJobWithConcatenatedInputs {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputUri1 = "gs://my-bucket/my-video-file1";
    Duration startTimeInput1 = Duration.newBuilder().setSeconds(0).setNanos(0).build();
    Duration endTimeInput1 = Duration.newBuilder().setSeconds(8).setNanos(100000000).build();
    String inputUri2 = "gs://my-bucket/my-video-file2";
    Duration startTimeInput2 = Duration.newBuilder().setSeconds(3).setNanos(500000000).build();
    Duration endTimeInput2 = Duration.newBuilder().setSeconds(15).setNanos(0).build();
    String outputUri = "gs://my-bucket/my-output-folder/";

    createJobWithConcatenatedInputs(
        projectId,
        location,
        inputUri1,
        startTimeInput1,
        endTimeInput1,
        inputUri2,
        startTimeInput2,
        endTimeInput2,
        outputUri);
  }

  // Creates a job from an ad-hoc configuration that concatenates two input videos.
  public static void createJobWithConcatenatedInputs(
      String projectId,
      String location,
      String inputUri1,
      Duration startTimeInput1,
      Duration endTimeInput1,
      String inputUri2,
      Duration startTimeInput2,
      Duration endTimeInput2,
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

      JobConfig config =
          JobConfig.newBuilder()
              .addInputs(Input.newBuilder().setKey("input1").setUri(inputUri1))
              .addInputs(Input.newBuilder().setKey("input2").setUri(inputUri2))
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
              .addEditList(
                  0, // Index in the edit list
                  EditAtom.newBuilder()
                      .setKey("atom1")
                      .addInputs("input1")
                      .setStartTimeOffset(startTimeInput1)
                      .setEndTimeOffset(endTimeInput1)
                      .build())
              .addEditList(
                  1, // Index in the edit list
                  EditAtom.newBuilder()
                      .setKey("atom2")
                      .addInputs("input2")
                      .setStartTimeOffset(startTimeInput2)
                      .setEndTimeOffset(endTimeInput2)
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
// [END transcoder_create_job_with_concatenated_inputs]
