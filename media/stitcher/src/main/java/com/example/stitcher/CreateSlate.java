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

// [START videostitcher_create_slate]

import com.google.cloud.video.stitcher.v1.CreateSlateRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.Slate;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSlate {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String slateId = "my-slate-id";
    String slateUri =
        "https://my-slate-uri/test.mp4"; // URI of an MP4 video with at least one audio track

    createSlate(projectId, location, slateId, slateUri);
  }

  // Creates a slate. Slates are content that can be served when there are gaps in a livestream
  // ad break that cannot be filled with a dynamically served ad. For more information, see
  // https://cloud.google.com/video-stitcher/docs/how-to/managing-slates.
  public static Slate createSlate(
      String projectId, String location, String slateId, String slateUri)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CreateSlateRequest createSlateRequest =
          CreateSlateRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setSlateId(slateId)
              .setSlate(Slate.newBuilder().setUri(slateUri).build())
              .build();

      Slate response =
          videoStitcherServiceClient
              .createSlateAsync(createSlateRequest)
              .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Created new slate: " + response.getName());
      return response;
    }
  }
}
// [END videostitcher_create_slate]
