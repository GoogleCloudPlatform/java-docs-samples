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

// [START transcoder_list_jobs]

import com.google.cloud.video.transcoder.v1beta1.Job;
import com.google.cloud.video.transcoder.v1beta1.ListJobsRequest;
import com.google.cloud.video.transcoder.v1beta1.LocationName;
import com.google.cloud.video.transcoder.v1beta1.TranscoderServiceClient;
import java.io.IOException;

public class ListJobs {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";

    listJobs(projectId, location);
  }

  // Lists the jobs for a given location.
  public static void listJobs(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TranscoderServiceClient transcoderServiceClient = TranscoderServiceClient.create()) {

      var listJobsRequest =
          ListJobsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      // Send the list jobs request and process the response.
      TranscoderServiceClient.ListJobsPagedResponse response =
          transcoderServiceClient.listJobs(listJobsRequest);
      System.out.println("Jobs:");

      for (Job job : response.iterateAll()) {
        System.out.println(job.getName());
      }
    }
  }
}
// [END transcoder_list_jobs]
