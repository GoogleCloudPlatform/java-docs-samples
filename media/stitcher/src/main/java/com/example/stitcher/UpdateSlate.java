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

// [START videostitcher_update_slate]

import com.google.cloud.video.stitcher.v1.Slate;
import com.google.cloud.video.stitcher.v1.SlateName;
import com.google.cloud.video.stitcher.v1.UpdateSlateRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateSlate {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String slateId = "my-slate-id";
    String slateUri =
        "https://my-slate-uri/test.mp4"; // URI of an MP4 video with at least one audio track

    updateSlate(projectId, location, slateId, slateUri);
  }

  // updateSlate updates the slate URI for an existing slate.
  public static void updateSlate(String projectId, String location, String slateId, String slateUri)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      UpdateSlateRequest updateSlateRequest =
          UpdateSlateRequest.newBuilder()
              .setSlate(
                  Slate.newBuilder()
                      .setName(SlateName.of(projectId, location, slateId).toString())
                      .setUri(slateUri)
                      .build())
              // Set the update mask to the uri field in the existing slate. You must set the mask
              // to the field you want to update.
              .setUpdateMask(FieldMask.newBuilder().addPaths("uri").build())
              .build();

      Slate response = videoStitcherServiceClient.updateSlate(updateSlateRequest);
      System.out.println("Updated slate: " + response.getName());
    }
  }
}
// [END videostitcher_update_slate]
