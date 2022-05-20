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

// [START livestream_update_channel]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.video.livestream.v1.Channel;
import com.google.cloud.video.livestream.v1.ChannelName;
import com.google.cloud.video.livestream.v1.InputAttachment;
import com.google.cloud.video.livestream.v1.InputName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.OperationMetadata;
import com.google.cloud.video.livestream.v1.UpdateChannelRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateChannel {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String channelId = "my-channel-id";
    String inputId = "my-input-id";

    updateChannel(projectId, location, channelId, inputId);
  }

  public static void updateChannel(
      String projectId, String location, String channelId, String inputId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var updateChannelRequest =
          UpdateChannelRequest.newBuilder()
              .setChannel(
                  Channel.newBuilder()
                      .setName(ChannelName.of(projectId, location, channelId).toString())
                      .addInputAttachments(
                          0,
                          InputAttachment.newBuilder()
                              .setKey("updated-input")
                              .setInput(InputName.of(projectId, location, inputId).toString())
                              .build()))
              .setUpdateMask(FieldMask.newBuilder().addPaths("input_attachments").build())
              .build();

      OperationFuture<Channel, OperationMetadata> call =
          livestreamServiceClient.updateChannelAsync(updateChannelRequest);
      Channel result = call.get(1, TimeUnit.MINUTES);
      System.out.println("Updated channel: " + result.getName());
    }
  }
}
// [END livestream_update_channel]
