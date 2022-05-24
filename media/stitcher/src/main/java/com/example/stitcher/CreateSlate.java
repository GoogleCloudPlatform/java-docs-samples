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

package com.example.stitcher;

// [START video_stitcher_create_slate]

import com.google.cloud.video.stitcher.v1.CreateSlateRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.Slate;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;

public class CreateSlate {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String slateId = "my-slate-id";
    String slateUri = "my-slate-uri"; // URI of an MP4 video with at least one audio track

    createSlate(projectId, location, slateId, slateUri);
  }

  public static void createSlate(String projectId, String location, String slateId, String slateUri)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      var createSlateRequest =
          CreateSlateRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setSlateId(slateId)
              .setSlate(Slate.newBuilder().setUri(slateUri).build())
              .build();

      Slate response = videoStitcherServiceClient.createSlate(createSlateRequest);
      System.out.println("Slate: " + response.getName());
    }
  }
}
// [END video_stitcher_create_slate]
