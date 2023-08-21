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

// [START dlp_inspect_send_data_to_hybrid_job_trigger]

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ActivateJobTriggerRequest;
import com.google.privacy.dlp.v2.Container;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.HybridContentItem;
import com.google.privacy.dlp.v2.HybridFindingDetails;
import com.google.privacy.dlp.v2.HybridInspectJobTriggerRequest;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.JobTriggerName;
import com.google.privacy.dlp.v2.ListDlpJobsRequest;

public class InspectDataToHybridJobTrigger {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The job trigger id used to for processing a hybrid job trigger.
    String jobTriggerId = "your-job-trigger-id";
    // The string to de-identify.
    String textToDeIdentify = "My email is test@example.org and my name is Gary.";
    inspectDataToHybridJobTrigger(textToDeIdentify, projectId, jobTriggerId);
  }

  // Inspects data using a hybrid job trigger.
  // Hybrid jobs trigger allows to scan payloads of data sent from virtually any source for
  // sensitive information and then store the findings in Google Cloud.
  public static void inspectDataToHybridJobTrigger(
      String textToDeIdentify, String projectId, String jobTriggerId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpClient = DlpServiceClient.create()) {
      // Specify the content to be inspected.
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToDeIdentify).build();

      // Contains metadata to associate with the content.
      // Refer to https://cloud.google.com/dlp/docs/reference/rest/v2/Container for specifying the
      // paths in container object.
      Container container =
          Container.newBuilder()
              .setFullPath("10.0.0.2:logs1:app1")
              .setRelativePath("app1")
              .setRootPath("10.0.0.2:logs1")
              .setType("logging_sys")
              .setVersion("1.2")
              .build();

      HybridFindingDetails hybridFindingDetails =
          HybridFindingDetails.newBuilder().setContainerDetails(container).build();

      HybridContentItem hybridContentItem =
          HybridContentItem.newBuilder()
              .setItem(contentItem)
              .setFindingDetails(hybridFindingDetails)
              .build();

      // Activate the job trigger.
      ActivateJobTriggerRequest activateJobTriggerRequest =
          ActivateJobTriggerRequest.newBuilder()
              .setName(JobTriggerName.of(projectId, jobTriggerId).toString())
              .build();

      DlpJob dlpJob;

      try {
        dlpJob = dlpClient.activateJobTrigger(activateJobTriggerRequest);
      } catch (InvalidArgumentException e) {
        ListDlpJobsRequest request =
            ListDlpJobsRequest.newBuilder()
                .setParent(JobTriggerName.of(projectId, jobTriggerId).toString())
                .setFilter("trigger_name=" + JobTriggerName.of(projectId, jobTriggerId).toString())
                .build();

        // Retrieve the DLP jobs triggered by the job trigger
        DlpServiceClient.ListDlpJobsPagedResponse response = dlpClient.listDlpJobs(request);
        dlpJob = response.getPage().getResponse().getJobs(0);
      }

      // Build the hybrid inspect request.
      HybridInspectJobTriggerRequest request =
          HybridInspectJobTriggerRequest.newBuilder()
              .setName(JobTriggerName.of(projectId, jobTriggerId).toString())
              .setHybridItem(hybridContentItem)
              .build();

      // Send the hybrid inspect request.
      dlpClient.hybridInspectJobTrigger(request);

      // Build a request to get the completed job
      GetDlpJobRequest getDlpJobRequest =
          GetDlpJobRequest.newBuilder().setName(dlpJob.getName()).build();

      DlpJob result = null;

      do {
        result = dlpClient.getDlpJob(getDlpJobRequest);
        Thread.sleep(5000);
      } while (result.getInspectDetails().getResult().getProcessedBytes() <= 0);

      System.out.println("Job status: " + result.getState());
      System.out.println("Job name: " + result.getName());
      // Parse the response and process results.
      InspectDataSourceDetails.Result inspectionResult = result.getInspectDetails().getResult();
      System.out.println("Findings: ");
      for (InfoTypeStats infoTypeStat : inspectionResult.getInfoTypeStatsList()) {
        System.out.println("\tInfoType: " + infoTypeStat.getInfoType().getName());
        System.out.println("\tCount: " + infoTypeStat.getCount() + "\n");
      }
    }
  }
}
// [END dlp_inspect_send_data_to_hybrid_job_trigger]
