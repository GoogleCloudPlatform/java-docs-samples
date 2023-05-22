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

// [START dlp_create_stored_infotype]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.BigQueryField;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.CreateStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import java.io.IOException;

public class CreateStoredInfoType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    //The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The path to the location in a GCS bucket to store the created dictionary.
    String outputPath = "gs://" + "your-bucket-name" + "path/to/directory";
    createStoredInfoType(projectId, outputPath);
  }

  // Creates a custom stored info type that contains GitHub usernames used in commits.
  public static void createStoredInfoType(String projectId, String outputPath)
      throws IOException {
    try (DlpServiceClient dlp = DlpServiceClient.create()) {

      // Optionally set a display name and a description.
      String displayName = "GitHub usernames";
      String description = "Dictionary of GitHub usernames used in commits";

      // The output path where the custom dictionary containing the GitHub usernames will be stored.
      CloudStoragePath cloudStoragePath =
          CloudStoragePath.newBuilder()
              .setPath(outputPath)
              .build();

      // The reference to the table containing the GitHub usernames.
      BigQueryTable table = BigQueryTable.newBuilder()
              .setProjectId("bigquery-public-data")
              .setDatasetId("samples")
              .setTableId("github_nested")
              .build();

      // The reference to the BigQuery field that contains the GitHub usernames.
      BigQueryField bigQueryField = BigQueryField.newBuilder()
              .setTable(table)
              .setField(FieldId.newBuilder().setName("actor").build())
              .build();

      LargeCustomDictionaryConfig largeCustomDictionaryConfig =
          LargeCustomDictionaryConfig.newBuilder()
              .setOutputPath(cloudStoragePath)
              .setBigQueryField(bigQueryField)
              .build();

      StoredInfoTypeConfig storedInfoTypeConfig = StoredInfoTypeConfig.newBuilder()
              .setDisplayName(displayName)
              .setDescription(description)
              .setLargeCustomDictionary(largeCustomDictionaryConfig)
              .build();

      // Combine configurations into a request for the service.
      CreateStoredInfoTypeRequest createStoredInfoType = CreateStoredInfoTypeRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setConfig(storedInfoTypeConfig)
              .setStoredInfoTypeId("github-usernames")
              .build();

      // Send the request and receive response from the service.
      StoredInfoType response = dlp.createStoredInfoType(createStoredInfoType);

      // Print the results.
      System.out.println("Created Stored InfoType: " + response.getName());
    }
  }
}

// [END dlp_create_stored_infotype]
