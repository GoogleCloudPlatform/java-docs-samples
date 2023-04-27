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

// [Start dlp_inspect_data_to_hybrid_job]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ActivateJobTriggerRequest;
import com.google.privacy.dlp.v2.Container;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.DlpJobName;
import com.google.privacy.dlp.v2.HybridContentItem;
import com.google.privacy.dlp.v2.HybridFindingDetails;
import com.google.privacy.dlp.v2.HybridInspectDlpJobRequest;
import com.google.privacy.dlp.v2.HybridInspectResponse;
import com.google.privacy.dlp.v2.JobTriggerName;
import java.util.HashMap;
import java.util.Map;

public class InspectDataToHybridJob {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The Job id and Job trigger id used to for processing a hybrid job trigger.
    String jobId = "your-job-id";
    String jobTriggerId = "your-job-trigger-id";
    // The string to de-identify.
    String textToDeIdentify = "My email is test@example.org";
    inspectDataToHybridJob(textToDeIdentify, projectId, jobId, jobTriggerId);
  }

  // HybridInspect request sent to Cloud DLP for processing by a hybrid job trigger.
  public static void inspectDataToHybridJob(
      String textToDeIdentify, String projectId, String jobId, String jobTriggerId)
      throws Exception {
    // create a DlpServiceClient instance
    try (DlpServiceClient dlpClient = DlpServiceClient.create()) {
      // Specify the content to be inspected.
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToDeIdentify).build();

      // Contains metadata to associate with the content.
      Container container =
          Container.newBuilder()
              .setFullPath("10.0.0.2:logs1:app1")
              .setRelativePath("app1")
              .setRootPath("10.0.0.2:logs1")
              .setType("logging_sys")
              .setVersion("1.2")
              .build();

      Map<String, String> labels = new HashMap<>();
      labels.put("env", "prod");
      labels.put("appointment-bookings-comments", "");

      HybridFindingDetails hybridFindingDetails =
          HybridFindingDetails.newBuilder()
              .setContainerDetails(container)
              .putAllLabels(labels)
              .build();

      // Build the hybrid content item.
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
      dlpClient.activateJobTrigger(activateJobTriggerRequest);

      // Build the hybrid inspect request.
      HybridInspectDlpJobRequest request =
          HybridInspectDlpJobRequest.newBuilder()
              .setName(DlpJobName.of(projectId, jobId).toString())
              .setHybridItem(hybridContentItem)
              .build();

      // Send the hybrid inspect request.
      HybridInspectResponse response = dlpClient.hybridInspectDlpJob(request);

      // Print the result.
      System.out.print(response);
    }
  }
}
// [END dlp_inspect_data_to_hybrid_job]
