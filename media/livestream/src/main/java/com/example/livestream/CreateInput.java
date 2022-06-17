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

// [START livestream_create_input]

import com.google.cloud.video.livestream.v1.CreateInputRequest;
import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LocationName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInput {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputId = "my-input-id";

    createInput(projectId, location, inputId);
  }

  public static void createInput(String projectId, String location, String inputId)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      var createInputRequest =
          CreateInputRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setInputId(inputId)
              .setInput(Input.newBuilder().setType(Input.Type.RTMP_PUSH).build())
              .build();

      Input result =
          livestreamServiceClient.createInputAsync(createInputRequest).get(1, TimeUnit.MINUTES);
      System.out.println("Input: " + result.getName());
    }
  }
}
// [END livestream_create_input]
