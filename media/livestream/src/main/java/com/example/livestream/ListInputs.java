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

// [START livestream_list_inputs]

import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.ListInputsRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LocationName;
import java.io.IOException;

public class ListInputs {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listInputs(projectId, location);
  }

  public static void listInputs(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      var listInputsRequest =
          ListInputsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      LivestreamServiceClient.ListInputsPagedResponse response =
          livestreamServiceClient.listInputs(listInputsRequest);
      System.out.println("Inputs:");

      for (Input input : response.iterateAll()) {
        System.out.println(input.getName());
      }
    }
  }
}
// [END livestream_list_inputs]
