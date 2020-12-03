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

// [START transcoder_delete_job_template]

import com.google.cloud.video.transcoder.v1beta1.DeleteJobTemplateRequest;
import com.google.cloud.video.transcoder.v1beta1.JobTemplateName;
import com.google.cloud.video.transcoder.v1beta1.TranscoderServiceClient;
import java.io.IOException;

public class DeleteJobTemplate {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String templateId = "my-job-template";

    deleteJobTemplate(projectId, location, templateId);
  }

  // Deletes a job template.
  public static void deleteJobTemplate(String projectId, String location, String templateId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TranscoderServiceClient transcoderServiceClient = TranscoderServiceClient.create()) {
      JobTemplateName jobTemplateName =
          JobTemplateName.newBuilder()
              .setProject(projectId)
              .setLocation(location)
              .setJobTemplate(templateId)
              .build();
      var deleteJobTemplateRequest =
          DeleteJobTemplateRequest.newBuilder().setName(jobTemplateName.toString()).build();

      // Send the delete job template request and process the response.
      transcoderServiceClient.deleteJobTemplate(deleteJobTemplateRequest);
      System.out.println("Deleted job template");
    }
  }
}
// [END transcoder_delete_job_template]
