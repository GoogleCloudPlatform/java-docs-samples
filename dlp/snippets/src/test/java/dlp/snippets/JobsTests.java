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
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.CloudStorageOptions;
import com.google.privacy.dlp.v2.CloudStorageOptions.FileSet;
import com.google.privacy.dlp.v2.Color;
import com.google.privacy.dlp.v2.CreateDeidentifyTemplateRequest;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyTemplate;
import com.google.privacy.dlp.v2.DeleteDeidentifyTemplateRequest;
import com.google.privacy.dlp.v2.DeleteDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.FieldTransformation;
import com.google.privacy.dlp.v2.ImageTransformations;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ProjectDeidentifyTemplateName;
import com.google.privacy.dlp.v2.RecordTransformations;
import com.google.privacy.dlp.v2.ReplaceValueConfig;
import com.google.privacy.dlp.v2.ReplaceWithInfoTypeConfig;
import com.google.privacy.dlp.v2.StorageConfig;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JobsTests extends TestBase {
  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT", "GCS_PATH");
  }

  private static DlpServiceClient dlpServiceClient;

  @BeforeClass
  public static void setUp() throws Exception {
    // Initialize the Dlp Service Client.
    dlpServiceClient = DlpServiceClient.create();
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

    return dlpServiceClient.createDlpJob(createDlpJobRequest);
  }

  private static void createBucket(String bucketName) {
    // Get the Storage service using the provided project ID.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();

    // Retrieve the bucket using the specified bucket name.
    Bucket bucket = storage.get(bucketName);

    // Create the bucket in Google Cloud Storage if it doesn't exist.
    if (bucket == null) {
      storage.create(BucketInfo.newBuilder(bucketName).build());
    }
  }

  private static void deleteBucket(String bucketName) {
    // Get the Storage service using the provided project ID.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();

    // List objects in the bucket
    Iterable<Blob> blobs = storage.list(bucketName).iterateAll();

    // Delete each object in the bucket
    for (Blob blob : blobs) {
      blob.delete();
    }
    // Retrieve the bucket using the specified bucket name.
    Bucket bucket = storage.get(bucketName);

    bucket.delete();
  }

  private static void createDeidentifyTemplate(String deidentifyTemplateId) {

    // Specify that findings should be replaced with corresponding info type name.
    ReplaceWithInfoTypeConfig replaceWithInfoTypeConfig =
        ReplaceWithInfoTypeConfig.getDefaultInstance();

    // Associate info type with the replacement strategy.
    InfoTypeTransformations infoTypeTransformations =
        InfoTypeTransformations.newBuilder()
            .addTransformations(
                InfoTypeTransformations.InfoTypeTransformation.newBuilder()
                    .setPrimitiveTransformation(
                        PrimitiveTransformation.newBuilder()
                            .setReplaceWithInfoTypeConfig(replaceWithInfoTypeConfig)
                            .build())
                    .build())
            .build();

    DeidentifyConfig deidentifyConfig =
        DeidentifyConfig.newBuilder().setInfoTypeTransformations(infoTypeTransformations).build();

    DeidentifyTemplate template =
        DeidentifyTemplate.newBuilder().setDeidentifyConfig(deidentifyConfig).build();

    // Construct the template creation request to be sent by the client.
    CreateDeidentifyTemplateRequest createRequest =
        CreateDeidentifyTemplateRequest.newBuilder()
            .setDeidentifyTemplate(template)
            .setParent(LocationName.of(PROJECT_ID, "global").toString())
            .setTemplateId(deidentifyTemplateId)
            .build();

    // Call DLP API to create de-identify template.
    dlpServiceClient.createDeidentifyTemplate(createRequest);
  }

  private static void createStructuredDeidentifyTemplate(String deidentifyStructuredTemplateId) {

    Value value = Value.newBuilder().setStringValue("Hello").build();

    // Specify that findings should be replaced with corresponding value.
    ReplaceValueConfig replaceValueConfig =
        ReplaceValueConfig.newBuilder().setNewValue(value).build();

    // Associate info type with the replacement strategy.
    RecordTransformations recordTransformations =
        RecordTransformations.newBuilder()
            .addFieldTransformations(
                FieldTransformation.newBuilder()
                    .setPrimitiveTransformation(
                        PrimitiveTransformation.newBuilder()
                            .setReplaceConfig(replaceValueConfig)
                            .build())
                    .build())
            .build();

    DeidentifyConfig deidentifyConfig =
        DeidentifyConfig.newBuilder().setRecordTransformations(recordTransformations).build();

    DeidentifyTemplate template =
        DeidentifyTemplate.newBuilder().setDeidentifyConfig(deidentifyConfig).build();

    // Construct the template creation request to be sent by the client.
    CreateDeidentifyTemplateRequest createRequest =
        CreateDeidentifyTemplateRequest.newBuilder()
            .setDeidentifyTemplate(template)
            .setParent(LocationName.of(PROJECT_ID, "global").toString())
            .setTemplateId(deidentifyStructuredTemplateId)
            .build();

    // Call DLP API to create de-identify template.
    dlpServiceClient.createDeidentifyTemplate(createRequest);
  }

  private static void createRedactImageTemplate(String redactImageTemplateId) {

    // Specify the color to use when redacting content from an image.
    ImageTransformations.ImageTransformation imageTransformation =
        ImageTransformations.ImageTransformation.newBuilder()
            .setRedactionColor(Color.newBuilder().setBlue(0).setGreen(0).setRed(1).build())
            .build();

    ImageTransformations imageTransformations =
        ImageTransformations.newBuilder().addTransforms(imageTransformation).build();

    DeidentifyConfig deidentifyConfig =
        DeidentifyConfig.newBuilder().setImageTransformations(imageTransformations).build();

    DeidentifyTemplate template =
        DeidentifyTemplate.newBuilder().setDeidentifyConfig(deidentifyConfig).build();

    // Construct the template creation request to be sent by the client.
    CreateDeidentifyTemplateRequest createRequest =
        CreateDeidentifyTemplateRequest.newBuilder()
            .setDeidentifyTemplate(template)
            .setParent(LocationName.of(PROJECT_ID, "global").toString())
            .setTemplateId(redactImageTemplateId)
            .build();

    // Call DLP API to create de-identify template.
    dlpServiceClient.createDeidentifyTemplate(createRequest);
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

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);
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

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);
  }

  @Test
  public void testListJobs() throws Exception {
    // Call listJobs to print out a list of jobIds.
    JobsList.listJobs(PROJECT_ID);
    String output = bout.toString();

    // Check that the output contains a list of jobs, or is empty.
    assertThat(output).contains("DLP jobs found:");
  }

  @Test
  public void testDeleteJobs() throws Exception {
    // Create a job with a unique UUID to be deleted.
    String jobId = UUID.randomUUID().toString();
    createJob(jobId);

    // Delete the job with the specified ID.
    JobsDelete.deleteJobs(PROJECT_ID, "i-" + jobId);
    String output = bout.toString();
    assertThat(output).contains("Job deleted successfully.");
  }

  @Test
  public void testInspectBigQuerySendToScc() throws Exception {
    InspectBigQuerySendToScc.inspectBigQuerySendToScc(PROJECT_ID, DATASET_ID, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Job created successfully");
    String dlpJobName = output.split("Job created successfully: ")[1].split("\n")[0];

    // Delete the created Dlp Job
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);
  }

  @Test
  public void testCreateDatastoreJobWithScc() throws Exception {
    InspectDatastoreSendToScc.inspectDatastoreSendToScc(
        PROJECT_ID, DATASTORE_NAMESPACE, DATASTORE_KIND);

    String output = bout.toString();
    assertThat(output).contains("Job created successfully");
    String dlpJobName = output.split("Job created successfully: ")[1].split("\n")[0];

    // Delete the created Dlp Job
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);
  }

  @Test
  public void testCreateJobsSendScc() throws Exception {
    // Call createJobs to create a Dlp job from project id and gcs path and send data to SCC.
    InspectGcsFileSendToScc.createJobSendToScc(PROJECT_ID, GCS_PATH);
    String output = bout.toString();
    assertThat(output).contains("Job created successfully:");

    // Delete the created Dlp Job
    String dlpJobName = output.split("Job created successfully: ")[1].split("\n")[0];
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);
  }

  @Test
  public void testDeidentifyStorage() throws Exception {
    // Create de-identify templates.
    String deidentifyTemplateId = UUID.randomUUID().toString();
    createDeidentifyTemplate(deidentifyTemplateId);

    String deidentifyStructuredTemplateId = UUID.randomUUID().toString();
    createStructuredDeidentifyTemplate(deidentifyStructuredTemplateId);

    String imageRedactTemplateId = UUID.randomUUID().toString();
    createRedactImageTemplate(imageRedactTemplateId);

    // Create the output directory in google cloud.
    String bucketName = UUID.randomUUID().toString();
    createBucket(bucketName);

    // De-identify cloud storage.
    DeidentifyCloudStorage.deidentifyCloudStorage(
        PROJECT_ID,
        GCS_PATH,
        TABLE_ID,
        DATASET_ID,
        "gs://" + bucketName,
        deidentifyTemplateId,
        deidentifyStructuredTemplateId,
        imageRedactTemplateId);

    // Assert the output.
    String output = bout.toString();
    assertThat(output).contains("Job status: DONE");
    assertThat(output).contains("Findings");
    assertThat(output).contains("Info type");

    // Delete the job.
    String dlpJobName =
        Arrays.stream(output.split("\n"))
            .filter(line -> line.contains("Job name:"))
            .findFirst()
            .get();

    dlpJobName = dlpJobName.split(":")[1].trim();

    // Delete DLP resources.
    DeleteDlpJobRequest deleteDlpJobRequest =
        DeleteDlpJobRequest.newBuilder().setName(dlpJobName).build();

    dlpServiceClient.deleteDlpJob(deleteDlpJobRequest);

    DeleteDeidentifyTemplateRequest deleteDeidentifyTemplateRequest =
        DeleteDeidentifyTemplateRequest.newBuilder()
            .setName(ProjectDeidentifyTemplateName.of(PROJECT_ID, deidentifyTemplateId).toString())
            .build();

    dlpServiceClient.deleteDeidentifyTemplate(deleteDeidentifyTemplateRequest);

    DeleteDeidentifyTemplateRequest deleteDeidentifyStructuredTemplateRequest =
        DeleteDeidentifyTemplateRequest.newBuilder()
            .setName(
                ProjectDeidentifyTemplateName.of(PROJECT_ID, deidentifyStructuredTemplateId)
                    .toString())
            .build();

    dlpServiceClient.deleteDeidentifyTemplate(deleteDeidentifyStructuredTemplateRequest);

    DeleteDeidentifyTemplateRequest deleteImageRedactTemplateRequest =
        DeleteDeidentifyTemplateRequest.newBuilder()
            .setName(ProjectDeidentifyTemplateName.of(PROJECT_ID, imageRedactTemplateId).toString())
            .build();

    dlpServiceClient.deleteDeidentifyTemplate(deleteImageRedactTemplateRequest);

    deleteBucket(bucketName);
  }
}
