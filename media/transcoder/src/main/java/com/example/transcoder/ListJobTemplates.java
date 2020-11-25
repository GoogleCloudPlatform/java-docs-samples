/*
 * Copyright 2020 Google LLC
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

package com.example.transcoder;

// [START transcoder_list_job_templates]

import com.google.cloud.video.transcoder.v1beta1.JobTemplate;
import com.google.cloud.video.transcoder.v1beta1.ListJobTemplatesRequest;
import com.google.cloud.video.transcoder.v1beta1.LocationName;
import com.google.cloud.video.transcoder.v1beta1.TranscoderServiceClient;
import java.io.IOException;

public class ListJobTemplates {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listJobTemplates(projectId, location);
  }

  // Lists the job templates for a given location.
  public static void listJobTemplates(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TranscoderServiceClient transcoderServiceClient = TranscoderServiceClient.create()) {

      var listJobTemplatesRequest =
          ListJobTemplatesRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      // Send the list job templates request and process the response.
      TranscoderServiceClient.ListJobTemplatesPagedResponse response =
          transcoderServiceClient.listJobTemplates(listJobTemplatesRequest);
      System.out.println("Job templates:");

      for (JobTemplate jobTemplate : response.iterateAll()) {
        System.out.println(jobTemplate.getName());
      }
    }
  }
}
// [END transcoder_list_job_templates]
