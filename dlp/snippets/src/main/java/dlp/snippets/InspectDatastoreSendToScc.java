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

// [START dlp_inspect_datastore_send_to_scc]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DatastoreOptions;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.KindExpression;
import com.google.privacy.dlp.v2.Likelihood;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PartitionId;
import com.google.privacy.dlp.v2.StorageConfig;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InspectDatastoreSendToScc {

  private static final int TIMEOUT_MINUTES = 15;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The namespace specifier to be used for the partition entity.
    String datastoreNamespace = "your-datastore-namespace";
    // The datastore kind defining a data set.
    String datastoreKind = "your-datastore-kind";
    inspectDatastoreSendToScc(projectId, datastoreNamespace, datastoreKind);
  }

  // Creates a DLP Job to scan the sample data stored in a DataStore table and save its scan results
  // to Security Command Center.
  public static void inspectDatastoreSendToScc(
      String projectId, String datastoreNamespace, String datastoreKind)
      throws IOException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Specify the Datastore entity to be inspected.
      PartitionId partitionId =
          PartitionId.newBuilder()
              .setProjectId(projectId)
              .setNamespaceId(datastoreNamespace)
              .build();

      KindExpression kindExpression = KindExpression.newBuilder().setName(datastoreKind).build();

      DatastoreOptions datastoreOptions =
          DatastoreOptions.newBuilder().setKind(kindExpression).setPartitionId(partitionId).build();

      StorageConfig storageConfig =
          StorageConfig.newBuilder().setDatastoreOptions(datastoreOptions).build();

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
// [END dlp_inspect_datastore_send_to_scc]
