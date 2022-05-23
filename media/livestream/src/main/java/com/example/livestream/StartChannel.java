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

// [START livestream_start_channel]

import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StartChannel {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";

    startChannel(projectId, location, channelId);
  }

  public static void startChannel(String projectId, String location, String channelId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      ChannelName name = ChannelName.of(projectId, location, channelId);
      livestreamServiceClient.startChannelAsync(name).get(1, TimeUnit.MINUTES);
      System.out.println("Started channel");
    }
  }
}
// [END livestream_start_channel]
