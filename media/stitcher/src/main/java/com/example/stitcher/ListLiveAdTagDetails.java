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

// [START videostitcher_list_live_ad_tag_details]

import com.google.cloud.video.stitcher.v1.ListLiveAdTagDetailsRequest;
import com.google.cloud.video.stitcher.v1.LiveAdTagDetail;
import com.google.cloud.video.stitcher.v1.LiveSessionName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListLiveAdTagDetailsPagedResponse;
import java.io.IOException;

public class ListLiveAdTagDetails {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String sessionId = "my-session-id";

    listLiveAdTagDetails(projectId, location, sessionId);
  }

  // Lists the live ad tag details for a given live session.
  public static ListLiveAdTagDetailsPagedResponse listLiveAdTagDetails(
      String projectId, String location, String sessionId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListLiveAdTagDetailsRequest listLiveAdTagDetailsRequest =
          ListLiveAdTagDetailsRequest.newBuilder()
              .setParent(LiveSessionName.of(projectId, location, sessionId).toString())
              .build();

      VideoStitcherServiceClient.ListLiveAdTagDetailsPagedResponse response =
          videoStitcherServiceClient.listLiveAdTagDetails(listLiveAdTagDetailsRequest);

      System.out.println("Live ad tag details:");
      for (LiveAdTagDetail adTagDetail : response.iterateAll()) {
        System.out.println(adTagDetail.toString());
      }
      return response;
    }
  }
}
// [END videostitcher_list_live_ad_tag_details]
