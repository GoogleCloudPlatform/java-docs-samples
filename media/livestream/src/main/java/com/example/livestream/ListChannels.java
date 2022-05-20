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

package com.example.livestream;

// [START livestream_list_channels]

import com.google.cloud.video.livestream.v1.Channel;
import com.google.cloud.video.livestream.v1.ListChannelsRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LocationName;
import java.io.IOException;

public class ListChannels {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listChannels(projectId, location);
  }

  public static void listChannels(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var listChannelsRequest =
          ListChannelsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      LivestreamServiceClient.ListChannelsPagedResponse response =
          livestreamServiceClient.listChannels(listChannelsRequest);
      System.out.println("Channels:");

      for (Channel channel : response.iterateAll()) {
        System.out.println(channel.getName());
      }
    }
  }
}
// [END livestream_list_channels]
