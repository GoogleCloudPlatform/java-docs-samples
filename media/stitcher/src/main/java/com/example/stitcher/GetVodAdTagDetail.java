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

// [START video_stitcher_get_vod_ad_tag_detail]

import com.google.cloud.video.stitcher.v1.GetVodAdTagDetailRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VodAdTagDetail;
import com.google.cloud.video.stitcher.v1.VodAdTagDetailName;
import java.io.IOException;

public class GetVodAdTagDetail {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String sessionId = "my-session-id";
    String adTagDetailId = "my-ad-tag-id";

    getVodAdTagDetail(projectId, location, sessionId, adTagDetailId);
  }

  public static void getVodAdTagDetail(
      String projectId, String location, String sessionId, String adTagDetailId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      GetVodAdTagDetailRequest getVodAdTagDetailRequest =
          GetVodAdTagDetailRequest.newBuilder()
              .setName(
                  VodAdTagDetailName.of(projectId, location, sessionId, adTagDetailId).toString())
              .build();

      VodAdTagDetail response =
          videoStitcherServiceClient.getVodAdTagDetail(getVodAdTagDetailRequest);
      System.out.println("VOD ad tag detail: " + response.getName());
    }
  }
}
// [END video_stitcher_get_vod_ad_tag_detail]
