/*
 * Copyright 2020 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.CloudStorageOptions;
import com.google.privacy.dlp.v2.CloudStorageOptions.FileSet;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.CreateJobTriggerRequest;
import com.google.privacy.dlp.v2.DeleteDlpJobRequest;
import com.google.privacy.dlp.v2.DeleteJobTriggerRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.HybridOptions;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.JobTrigger;
import com.google.privacy.dlp.v2.JobTriggerName;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.Manual;
import com.google.privacy.dlp.v2.StorageConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JobsTests extends TestBase {

  private static DlpJob createJob(String jobId) throws IOException {
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      FileSet fileSet = FileSet.newBuilder().setUrl(GCS_PATH).build();
      CloudStorageOptions cloudStorageOptions =
          CloudStorageOptions.newBuilder().setFileSet(fileSet).build();
      StorageConfig storageConfig =
          StorageConfig.newBuilder().setCloudStorageOptions(cloudStorageOptions).build();

      InspectJobConfig inspectJobConfig =
          InspectJobConfig.newBuilder()
              .setStorageConfig(storageConfig)
              .setInspectConfig(InspectConfig.newBuilder().build())
              .build();

      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(LocationName.of(PROJECT_ID, "global").toString())
              .setInspectJob(inspectJobConfig)
              .setJobId(jobId)
              .build();

      return dlp.createDlpJob(createDlpJobRequest);
    }
  }

  private static void createHybridJobTrigger(String jobTriggerId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set hybrid options for content outside GCP.
      HybridOptions hybridOptions =
          HybridOptions.newBuilder()
              .putLabels("env", "prod")
              .build();

      // Set storage config indicating the type of cloud storage.
      StorageConfig storageConfig =
          StorageConfig.newBuilder().setHybridOptions(hybridOptions).build();

      // Specify the type of info the inspection will look for.
      List<InfoType> infoTypes = new ArrayList<>();
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      for (String typeName : new String[] {"PERSON_NAME", "EMAIL_ADDRESS"}) {
        infoTypes.add(InfoType.newBuilder().setName(typeName).build());
      }

      // Specify how the content should be inspected.
      InspectConfig inspectConfig =
          InspectConfig.newBuilder().addAllInfoTypes(infoTypes).setIncludeQuote(true).build();
      // Configure the inspection job we want the service to perform.
      InspectJobConfig inspectJobConfig =
          InspectJobConfig.newBuilder()
              .setInspectConfig(inspectConfig)
              .setStorageConfig(storageConfig)
              .build();

      // Configure the hybrid job trigger to be created.
      JobTrigger.Trigger trigger =
          JobTrigger.Trigger.newBuilder().setManual(Manual.newBuilder().build()).build();

      JobTrigger jobTrigger =
          JobTrigger.newBuilder().addTriggers(trigger).setInspectJob(inspectJobConfig).build();

      // Construct the job trigger creation request.
      CreateJobTriggerRequest createDlpJobRequest =
          CreateJobTriggerRequest.newBuilder()
              .setParent(LocationName.of(PROJECT_ID, "global").toString())
              .setJobTrigger(jobTrigger)
              .setTriggerId(jobTriggerId)
              .build();

      // Send the job creation request and process the response.
      dlp.createJobTrigger(createDlpJobRequest);
    }
  }

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT", "GCS_PATH");
  }

  @Test
  public void testCreateJobs() throws Exception {
    // Call createJobs to create a Dlp job from project id and gcs path.
    JobsCreate.createJobs(PROJECT_ID, GCS_PATH);
    String output = bout.toString();
    assertThat(output).contains("Job created successfully:");

    // Delete the created Dlp Job
    String dlpJobName = output.split("Job created successfully: ")[1].split("\n")[0];
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();
    try (DlpServiceClient client = DlpServiceClient.create()) {
      client.deleteDlpJob(deleteDlpJobRequest);
    }
  }

  @Test
  public void testGetJobs() throws Exception {
    // Create a job with a unique UUID to be gotten
    String jobId = UUID.randomUUID().toString();
    DlpJob createdDlpJob = createJob(jobId);

    // Get the job with the specified ID
    JobsGet.getJobs(PROJECT_ID, "i-" + jobId);
    String output = bout.toString();
    assertThat(output).contains("Job got successfully.");

    // Delete the created Dlp Job
    String dlpJobName = createdDlpJob.getName();
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();
    try (DlpServiceClient client = DlpServiceClient.create()) {
      client.deleteDlpJob(deleteDlpJobRequest);
    }
  }

  @Test
  public void testListJobs() throws Exception {
    // Call listJobs to print out a list of jobIds
    JobsList.listJobs(PROJECT_ID);
    String output = bout.toString();

    // Check that the output contains a list of jobs, or is empty
    assertThat(output).contains("DLP jobs found:");
  }

  @Test
  public void testDeleteJobs() throws Exception {
    // Create a job with a unique UUID to be deleted
    String jobId = UUID.randomUUID().toString();
    createJob(jobId);

    // Delete the job with the specified ID
    JobsDelete.deleteJobs(PROJECT_ID, "i-" + jobId);
    String output = bout.toString();
    assertThat(output).contains("Job deleted successfully.");
  }

  @Test
  public void testInspectDataToHybridJobTrigger() throws Exception {
    // Create a job trigger with a unique UUID.
    String jobTriggerId = UUID.randomUUID().toString();
    createHybridJobTrigger(jobTriggerId);

    String textToDeIdentify = "My email is test@example.org";
    InspectDataToHybridJobTrigger.inspectDataToHybridJobTrigger(
        textToDeIdentify, PROJECT_ID, jobTriggerId);
    String output = bout.toString();
    assertThat(output).contains("Successfully inspected data using a hybrid job trigger");

    // Delete the specific job trigger.
    DeleteJobTriggerRequest deleteJobTriggerRequest =
        DeleteJobTriggerRequest.newBuilder()
            .setName(JobTriggerName.of(PROJECT_ID, jobTriggerId).toString())
            .build();
    try (DlpServiceClient client = DlpServiceClient.create()) {
      client.deleteJobTrigger(deleteJobTriggerRequest);
    }
  }
}
