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

// [START videostitcher_list_cdn_keys]

import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.ListCdnKeysRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListCdnKeysPagedResponse;
import java.io.IOException;

public class ListCdnKeys {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listCdnKeys(projectId, location);
  }

  // Lists the CDN keys for a given project and location.
  public static ListCdnKeysPagedResponse listCdnKeys(String projectId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      ListCdnKeysRequest listCdnKeysRequest =
          ListCdnKeysRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      VideoStitcherServiceClient.ListCdnKeysPagedResponse response =
          videoStitcherServiceClient.listCdnKeys(listCdnKeysRequest);

      System.out.println("CDN keys:");
      for (CdnKey cdnKey : response.iterateAll()) {
        System.out.println(cdnKey.getName());
      }
      return response;
    }
  }
}
// [END videostitcher_list_cdn_keys]
