/*
 * Copyright 2024 Google LLC
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

// [START videostitcher_list_vod_configs]

import com.google.cloud.video.stitcher.v1.ListVodConfigsRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListVodConfigsPagedResponse;
import com.google.cloud.video.stitcher.v1.VodConfig;
import java.io.IOException;

public class ListVodConfigs {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listVodConfigs(projectId, location);
  }

  // Lists all the video on demand (VOD) configs for a given project and locatin.
  public static ListVodConfigsPagedResponse listVodConfigs(String projectId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListVodConfigsRequest listVodConfigsRequest =
          ListVodConfigsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      VideoStitcherServiceClient.ListVodConfigsPagedResponse response =
          videoStitcherServiceClient.listVodConfigs(listVodConfigsRequest);

      System.out.println("VOD configs:");
      for (VodConfig vodConfig : response.iterateAll()) {
        System.out.println(vodConfig.getName());
      }
      return response;
    }
  }
}
// [END videostitcher_list_vod_configs]
