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

// [START dlp_deidentify_cloud_storage]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CloudStorageOptions;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.FileType;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.ProjectDeidentifyTemplateName;
import com.google.privacy.dlp.v2.StorageConfig;
import com.google.privacy.dlp.v2.TransformationConfig;
import com.google.privacy.dlp.v2.TransformationDetailsStorageConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeidentifyCloudStorage {
  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The name of the file in the bucket
    String gcsUri = "gs://" + "your-bucket-name" + "/path/to/your/file.txt";
    // The dataset id to be used and the reference table name to be inspected.
    String tableId = "your-bigquery-table-id";
    String datasetId = "your-bigquery-dataset-id";
    // The output directory used to store the result in the cloud storage.
    String outputDirectory = "your-output-directory";
    // The templates to be used for de-identification.
    String deidentifyTemplateId = "your-deidentify-template-id";
    String structuredDeidentifyTemplateId = "your-structured-deidentify-template-id";
    String imageRedactTemplateId = "your-image-redact-template-id";
    deidentifyCloudStorage(
        projectId,
        gcsUri,
        tableId,
        datasetId,
        outputDirectory,
        deidentifyTemplateId,
        structuredDeidentifyTemplateId,
        imageRedactTemplateId);
  }

  public static void deidentifyCloudStorage(
      String projectId,
      String gcsUri,
      String tableId,
      String datasetId,
      String outputDirectory,
      String deidentifyTemplateId,
      String structuredDeidentifyTemplateId,
      String imageRedactTemplateId)
      throws IOException, InterruptedException {

    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set path in Cloud Storage.
      CloudStorageOptions cloudStorageOptions =
          CloudStorageOptions.newBuilder()
              .setFileSet(CloudStorageOptions.FileSet.newBuilder().setUrl(gcsUri))
              .build();

      // Set storage config indicating the type of cloud storage.
      StorageConfig storageConfig =
          StorageConfig.newBuilder().setCloudStorageOptions(cloudStorageOptions).build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      List<InfoType> infoTypes = new ArrayList<>();
      for (String typeName : new String[] {"PERSON_NAME", "EMAIL_ADDRESS"}) {
        infoTypes.add(InfoType.newBuilder().setName(typeName).build());
      }

      InspectConfig inspectConfig =
          InspectConfig.newBuilder().addAllInfoTypes(infoTypes).setIncludeQuote(true).build();

      // Types of files to include for de-identification.
      List<FileType> fileTypesToTransform =
          Arrays.asList(
              FileType.valueOf("IMAGE"), FileType.valueOf("CSV"), FileType.valueOf("TEXT_FILE"));

      // Specify the big query table to store the transformation details.
      BigQueryTable table =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setTableId(tableId)
              .setDatasetId(datasetId)
              .build();

      TransformationDetailsStorageConfig transformationDetailsStorageConfig =
          TransformationDetailsStorageConfig.newBuilder().setTable(table).build();

      // Specify the de-identify template used for the transformation.
      TransformationConfig transformationConfig =
          TransformationConfig.newBuilder()
              .setDeidentifyTemplate(
                  ProjectDeidentifyTemplateName.of(projectId, deidentifyTemplateId).toString())
              .setImageRedactTemplate(
                  ProjectDeidentifyTemplateName.of(projectId, imageRedactTemplateId).toString())
              .setStructuredDeidentifyTemplate(
                  ProjectDeidentifyTemplateName.of(projectId, structuredDeidentifyTemplateId)
                      .toString())
              .build();

      Action.Deidentify deidentify =
          Action.Deidentify.newBuilder()
              .setCloudStorageOutput(outputDirectory)
              .setTransformationConfig(transformationConfig)
              .setTransformationDetailsStorageConfig(transformationDetailsStorageConfig)
              .addAllFileTypesToTransform(fileTypesToTransform)
              .build();

      Action action = Action.newBuilder().setDeidentify(deidentify).build();

      // Configure the long-running job we want the service to perform.
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

      // Send the job creation request.
      DlpJob response = dlp.createDlpJob(createDlpJobRequest);

      // Check if the job state is DONE.
      while (response.getState() != DlpJob.JobState.DONE) {
        // Sleep for 1 second.
        Thread.sleep(1000);

        // Get the updated job status.
        response = dlp.getDlpJob(response.getName());
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
// [END dlp_deidentify_cloud_storage]
