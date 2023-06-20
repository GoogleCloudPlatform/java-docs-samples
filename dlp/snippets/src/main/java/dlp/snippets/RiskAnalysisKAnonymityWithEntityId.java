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

// [START dlp_k_anonymity_with_entity_id]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.Action.SaveFindings;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityEquivalenceClass;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityHistogramBucket;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.EntityId;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.OutputStorageConfig;
import com.google.privacy.dlp.v2.PrivacyMetric;
import com.google.privacy.dlp.v2.PrivacyMetric.KAnonymityConfig;
import com.google.privacy.dlp.v2.RiskAnalysisJobConfig;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class RiskAnalysisKAnonymityWithEntityId {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The BigQuery dataset id to be used and the reference table name to be inspected.
    String datasetId = "your-bigquery-dataset-id";
    String tableId = "your-bigquery-table-id";
    calculateKAnonymityWithEntityId(projectId, datasetId, tableId);
  }

  // Uses the Data Loss Prevention API to compute the k-anonymity of a column set in a Google
  // BigQuery table.
  public static void calculateKAnonymityWithEntityId(
      String projectId, String datasetId, String tableId) throws IOException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Specify the BigQuery table to analyze
      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      // These values represent the column names of quasi-identifiers to analyze
      List<String> quasiIds = Arrays.asList("Age", "Mystery");

      // Create a list of FieldId objects based on the provided list of column names.
      List<FieldId> quasiIdFields =
          quasiIds.stream()
              .map(columnName -> FieldId.newBuilder().setName(columnName).build())
              .collect(Collectors.toList());

      // Specify the unique identifier in the source table for the k-anonymity analysis.
      FieldId uniqueIdField = FieldId.newBuilder().setName("Name").build();
      EntityId entityId = EntityId.newBuilder().setField(uniqueIdField).build();
      KAnonymityConfig kanonymityConfig = KAnonymityConfig.newBuilder()
              .addAllQuasiIds(quasiIdFields)
              .setEntityId(entityId)
              .build();

      // Configure the privacy metric to compute for re-identification risk analysis.
      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder().setKAnonymityConfig(kanonymityConfig).build();

      // Specify the bigquery table to store the findings.
      // The "test_results" table in the given BigQuery dataset will be created if it doesn't
      // already exist.
      BigQueryTable outputbigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId("test_results")
              .build();

      // Create action to publish job status notifications to BigQuery table.
      OutputStorageConfig outputStorageConfig =
          OutputStorageConfig.newBuilder().setTable(outputbigQueryTable).build();
      SaveFindings findings =
          SaveFindings.newBuilder().setOutputConfig(outputStorageConfig).build();
      Action action = Action.newBuilder().setSaveFindings(findings).build();

      // Configure the risk analysis job to perform
      RiskAnalysisJobConfig riskAnalysisJobConfig =
          RiskAnalysisJobConfig.newBuilder()
              .setSourceTable(bigQueryTable)
              .setPrivacyMetric(privacyMetric)
              .addActions(action)
              .build();

      // Build the request to be sent by the client
      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setRiskJob(riskAnalysisJobConfig)
              .build();

      // Send the request to the API using the client
      DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);

      // Build a request to get the completed job
      GetDlpJobRequest getDlpJobRequest =
          GetDlpJobRequest.newBuilder().setName(dlpJob.getName()).build();

      DlpJob completedJob = null;
      // Wait for job completion
      try {
        Duration timeout = Duration.ofMinutes(15);
        long startTime = System.currentTimeMillis();
        do {
          completedJob = dlpServiceClient.getDlpJob(getDlpJobRequest);
          TimeUnit.SECONDS.sleep(30);
        } while (completedJob.getState() != DlpJob.JobState.DONE
            && System.currentTimeMillis() - startTime <= timeout.toMillis());
      } catch (InterruptedException e) {
        System.out.println("Job did not complete within 15 minutes.");
      }

      // Retrieve completed job status
      System.out.println("Job status: " + completedJob.getState());
      System.out.println("Job name: " + dlpJob.getName());

      // Get the result and parse through and process the information
      KAnonymityResult kanonymityResult = completedJob.getRiskDetails().getKAnonymityResult();
      for (KAnonymityHistogramBucket result :
          kanonymityResult.getEquivalenceClassHistogramBucketsList()) {
        System.out.printf(
            "Bucket size range: [%d, %d]\n",
            result.getEquivalenceClassSizeLowerBound(), result.getEquivalenceClassSizeUpperBound());

        for (KAnonymityEquivalenceClass bucket : result.getBucketValuesList()) {
          List<String> quasiIdValues =
              bucket.getQuasiIdsValuesList().stream()
                  .map(Value::toString)
                  .collect(Collectors.toList());

          System.out.println("\tQuasi-ID values: " + String.join(", ", quasiIdValues));
          System.out.println("\tClass size: " + bucket.getEquivalenceClassSize());
        }
      }
    }
  }
}

// [END dlp_k_anonymity_with_entity_id]
