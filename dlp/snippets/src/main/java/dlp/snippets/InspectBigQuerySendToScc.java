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

// [START dlp_inspect_bigquery_send_to_scc]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.BigQueryOptions;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.Likelihood;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.StorageConfig;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InspectBigQuerySendToScc {

  private static final int TIMEOUT_MINUTES = 15;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The BigQuery dataset id to be used and the reference table name to be inspected.
    String bigQueryDatasetId = "your-project-bigquery-dataset";
    String bigQueryTableId = "your-project-bigquery_table";
    inspectBigQuerySendToScc(projectId, bigQueryDatasetId, bigQueryTableId);
  }

  // Inspects a BigQuery Table to send data to Security Command Center.
  public static void inspectBigQuerySendToScc(
      String projectId, String bigQueryDatasetId, String bigQueryTableId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Specify the BigQuery table to be inspected.
      BigQueryTable tableReference =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(bigQueryDatasetId)
              .setTableId(bigQueryTableId)
              .build();

      BigQueryOptions bigQueryOptions =
          BigQueryOptions.newBuilder().setTableReference(tableReference).build();

      StorageConfig storageConfig =
          StorageConfig.newBuilder().setBigQueryOptions(bigQueryOptions).build();

      // Specify the type of info the inspection will look for.
      List<InfoType> infoTypes =
          Stream.of("EMAIL_ADDRESS", "PERSON_NAME", "LOCATION", "PHONE_NUMBER")
              .map(it -> InfoType.newBuilder().setName(it).build())
              .collect(Collectors.toList());

      // The minimum likelihood required before returning a match.
      Likelihood minLikelihood = Likelihood.UNLIKELY;

      // The maximum number of findings to report (0 = server maximum)
      InspectConfig.FindingLimits findingLimits =
          InspectConfig.FindingLimits.newBuilder().setMaxFindingsPerItem(100).build();

      // Specify how the content should be inspected.
      InspectConfig inspectConfig =
          InspectConfig.newBuilder()
              .addAllInfoTypes(infoTypes)
              .setIncludeQuote(true)
              .setMinLikelihood(minLikelihood)
              .setLimits(findingLimits)
              .build();

      // Specify the action that is triggered when the job completes.
      Action.PublishSummaryToCscc publishSummaryToCscc =
          Action.PublishSummaryToCscc.getDefaultInstance();
      Action action = Action.newBuilder().setPublishSummaryToCscc(publishSummaryToCscc).build();

      // Configure the inspection job we want the service to perform.
      InspectJobConfig inspectJobConfig =
          InspectJobConfig.newBuilder()
              .setInspectConfig(inspectConfig)
              .setStorageConfig(storageConfig)
              .addActions(action)
              .build();

      // Construct the job creation request to be sent by the client.
      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setInspectJob(inspectJobConfig)
              .build();

      // Send the job creation request and process the response.
      DlpJob response = dlpServiceClient.createDlpJob(createDlpJobRequest);

      // Get the current time.
      long startTime = System.currentTimeMillis();

      // Check if the job state is DONE.
      while (response.getState() != DlpJob.JobState.DONE) {
        // Sleep for 30 second.
        Thread.sleep(30000);

        // Get the updated job status.
        response = dlpServiceClient.getDlpJob(response.getName());

        // Check if the timeout duration has exceeded.
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (TimeUnit.MILLISECONDS.toMinutes(elapsedTime) >= TIMEOUT_MINUTES) {
          System.out.printf("Job did not complete within %d minutes.%n", TIMEOUT_MINUTES);
          break;
        }
      }
      // Print the results.
      System.out.println("Job status: " + response.getState());
      System.out.println("Job name: " + response.getName());
      InspectDataSourceDetails.Result result = response.getInspectDetails().getResult();
      System.out.println("Findings: ");
      for (InfoTypeStats infoTypeStat : result.getInfoTypeStatsList()) {
        System.out.print("\tInfo type: " + infoTypeStat.getInfoType().getName());
        System.out.println("\tCount: " + infoTypeStat.getCount());
      }
    }
  }
}
// [END dlp_inspect_bigquery_send_to_scc]
