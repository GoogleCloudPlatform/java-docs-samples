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

package com.example.livestream;

// [START livestream_delete_channel_clip]

import com.google.cloud.video.livestream.v1.ClipName;
import com.google.cloud.video.livestream.v1.DeleteClipRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteChannelClip {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String clipId = "my-channel-clip-id";

    deleteChannelClip(projectId, location, channelId, clipId);
  }

  public static void deleteChannelClip(
      String projectId, String location, String channelId, String clipId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      DeleteClipRequest deleteClipRequest =
          DeleteClipRequest.newBuilder()
              .setName(ClipName.of(projectId, location, channelId, clipId).toString())
              .build();

      livestreamServiceClient.deleteClipAsync(deleteClipRequest).get(10, TimeUnit.MINUTES);
      System.out.println("Deleted channel clip");
    }
  }
}
// [END livestream_delete_channel_clip]