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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.CloudStorageOptions;
import com.google.privacy.dlp.v2.CloudStorageOptions.FileSet;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DeleteDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.HybridInspectJobTriggerRequest;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.StorageConfig;
import java.io.IOException;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class JobsTests extends TestBase {

  private static DlpServiceClient DLP_SERVICE_CLIENT;

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT", "GCS_PATH");
  }

  @BeforeClass
  public static void setUp() throws Exception {
    // Initialize the Dlp Service Client.
    DLP_SERVICE_CLIENT = DlpServiceClient.create();
  }

  private static DlpJob createJob(String jobId) throws IOException {

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

    return DLP_SERVICE_CLIENT.createDlpJob(createDlpJobRequest);
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

    DLP_SERVICE_CLIENT.deleteDlpJob(deleteDlpJobRequest);
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

    DLP_SERVICE_CLIENT.deleteDlpJob(deleteDlpJobRequest);
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
  public void testInspectBigQuerySendToScc() throws Exception {

    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);

    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      InfoTypeStats infoTypeStats =
              InfoTypeStats.newBuilder()
                      .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
                      .setCount(1)
                      .build();
      DlpJob dlpJob =
              DlpJob.newBuilder()
                      .setName("projects/project_id/locations/global/dlpJobs/job_id")
                      .setState(DlpJob.JobState.DONE)
                      .setInspectDetails(
                              InspectDataSourceDetails.newBuilder()
                                      .setResult(
                                              InspectDataSourceDetails.Result.newBuilder()
                                                      .setProcessedBytes(200)
                                                      .addInfoTypeStats(infoTypeStats)
                                                      .build()))
                      .build();
      when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
      InspectBigQuerySendToScc.inspectBigQuerySendToScc(
          "bigquery-public-data", "usa_names", "usa_1910_current");
      String output = bout.toString();
      assertThat(output).contains("Job status: DONE");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      assertThat(output).contains("Info type: EMAIL_ADDRESS");
      verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
    }
  }

  @Test
  public void testCreateDatastoreJobWithScc() throws Exception {

    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);

    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      InfoTypeStats infoTypeStats =
          InfoTypeStats.newBuilder()
              .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
              .setCount(1)
              .build();
      DlpJob dlpJob =
          DlpJob.newBuilder()
              .setName("projects/project_id/locations/global/dlpJobs/job_id")
              .setState(DlpJob.JobState.DONE)
              .setInspectDetails(
                  InspectDataSourceDetails.newBuilder()
                      .setResult(
                          InspectDataSourceDetails.Result.newBuilder()
                              .addInfoTypeStats(infoTypeStats)
                              .build()))
              .build();
      when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
      InspectDatastoreSendToScc.inspectDatastoreSendToScc(
          "project_id", "datastore_namespace_test", "datastore_kind_test");
      String output = bout.toString();
      assertThat(output).contains("Job status: DONE");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      assertThat(output).contains("Info type: EMAIL_ADDRESS");
      verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
    }
  }

  @Test
  public void testCreateJobsSendScc() throws Exception {

    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      InfoTypeStats infoTypeStats =
          InfoTypeStats.newBuilder()
              .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
              .setCount(1)
              .build();
      DlpJob dlpJob =
          DlpJob.newBuilder()
              .setName("projects/project_id/locations/global/dlpJobs/job_id")
              .setState(DlpJob.JobState.DONE)
              .setInspectDetails(
                  InspectDataSourceDetails.newBuilder()
                      .setResult(
                          InspectDataSourceDetails.Result.newBuilder()
                              .addInfoTypeStats(infoTypeStats)
                              .build()))
              .build();
      when(dlpServiceClient.createDlpJob(any())).thenReturn(dlpJob);
      InspectGcsFileSendToScc.createJobSendToScc("project_id", "gs://bucket_name/test.txt");
      String output = bout.toString();
      assertThat(output).contains("Job status: DONE");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      assertThat(output).contains("Info type: EMAIL_ADDRESS");
      verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
    }
  }

  @Test
  public void testInspectDataToHybridJobTrigger() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    String textToDeIdentify = "My email is test@example.org and my name is Gary.";
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      InfoTypeStats infoTypeStats =
          InfoTypeStats.newBuilder()
              .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
              .setCount(1)
              .build();
      DlpJob dlpJob =
          DlpJob.newBuilder()
              .setName("projects/project_id/locations/global/dlpJobs/job_id")
              .setState(DlpJob.JobState.DONE)
              .setInspectDetails(
                  InspectDataSourceDetails.newBuilder()
                      .setResult(
                          InspectDataSourceDetails.Result.newBuilder()
                              .setProcessedBytes(200)
                              .addInfoTypeStats(infoTypeStats)
                              .build()))
              .build();
      when(dlpServiceClient.activateJobTrigger(any())).thenReturn(dlpJob);
      when(dlpServiceClient.hybridInspectJobTrigger(any(HybridInspectJobTriggerRequest.class)))
          .thenReturn(null);
      when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
      InspectDataToHybridJobTrigger.inspectDataToHybridJobTrigger(
          textToDeIdentify, "project_id", "job_trigger_id");
      String output = bout.toString();
      assertThat(output).contains("Job status: DONE");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      assertThat(output).contains("InfoType: EMAIL_ADDRESS");
      verify(dlpServiceClient, times(1)).activateJobTrigger(any());
      verify(dlpServiceClient, times(1))
          .hybridInspectJobTrigger(any(HybridInspectJobTriggerRequest.class));
      verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
    }
  }
}
