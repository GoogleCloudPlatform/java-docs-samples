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

// [START livestream_stop_channel]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.ChannelOperationResponse;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.OperationMetadata;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopChannel {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";

    stopChannel(projectId, location, channelId);
  }

  public static void stopChannel(String projectId, String location, String channelId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      ChannelName name = ChannelName.of(projectId, location, channelId);
      OperationFuture<ChannelOperationResponse, OperationMetadata> call =
          livestreamServiceClient.stopChannelAsync(name);
      call.get(1, TimeUnit.MINUTES);
      System.out.println("Stopped channel");
    }
  }
}
// [END livestream_stop_channel]
