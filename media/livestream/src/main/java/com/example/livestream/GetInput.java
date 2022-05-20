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

// [START livestream_get_input]

import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.InputName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;

public class GetInput {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputId = "my-input-id";

    getInput(projectId, location, inputId);
  }

  public static void getInput(String projectId, String location, String inputId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      InputName name = InputName.of(projectId, location, inputId);
      Input response = livestreamServiceClient.getInput(name);
      System.out.println("Input: " + response.getName());
    }
  }
}
// [END livestream_get_input]
