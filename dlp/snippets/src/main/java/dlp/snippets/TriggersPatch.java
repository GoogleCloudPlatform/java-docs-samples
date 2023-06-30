/*
 * Copyright 2023 Google LLC
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

package dlp.snippets;

// [START dlp_update_trigger]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.JobTrigger;
import com.google.privacy.dlp.v2.JobTriggerName;
import com.google.privacy.dlp.v2.Likelihood;
import com.google.privacy.dlp.v2.UpdateJobTriggerRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class TriggersPatch {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The name of the job trigger to be updated.
    String jobTriggerName = "your-job-trigger-name";
    patchTrigger(projectId, jobTriggerName);
  }

  // Uses the Data Loss Prevention API to update an existing job trigger.
  public static void patchTrigger(String projectId, String jobTriggerName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      InfoType infoType = InfoType.newBuilder().setName("PERSON_NAME").build();

      InspectConfig inspectConfig = InspectConfig.newBuilder()
              .addInfoTypes(infoType)
              .setMinLikelihood(Likelihood.LIKELY)
              .build();

      InspectJobConfig inspectJobConfig = InspectJobConfig.newBuilder()
              .setInspectConfig(inspectConfig)
              .build();

      JobTrigger jobTrigger = JobTrigger.newBuilder()
              .setInspectJob(inspectJobConfig)
              .build();

      // Specify fields of the jobTrigger resource to be updated when the job trigger is modified.
      // Refer https://protobuf.dev/reference/protobuf/google.protobuf/#field-mask for constructing the field mask paths.
      FieldMask fieldMask = FieldMask.newBuilder()
              .addPaths("inspect_job.inspect_config.info_types")
              .addPaths("inspect_job.inspect_config.min_likelihood")
              .build();

      // Update the job trigger with the new configuration.
      UpdateJobTriggerRequest updateJobTriggerRequest = UpdateJobTriggerRequest.newBuilder()
              .setName(JobTriggerName.of(projectId, jobTriggerName).toString())
              .setJobTrigger(jobTrigger)
              .setUpdateMask(fieldMask)
              .build();

      // Call the API to update the job trigger.
      JobTrigger updatedJobTrigger = dlpServiceClient.updateJobTrigger(updateJobTriggerRequest);

      System.out.println("Job Trigger Name: " + updatedJobTrigger.getName());
      System.out.println(
          "InfoType updated: "
              + updatedJobTrigger.getInspectJob().getInspectConfig().getInfoTypes(0).getName());
      System.out.println(
          "Likelihood updated: "
              + updatedJobTrigger.getInspectJob().getInspectConfig().getMinLikelihood());
    }
  }
}

// [END dlp_update_trigger]
