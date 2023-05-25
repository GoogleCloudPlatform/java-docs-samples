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

// [START dlp_update_stored_infotype]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.CloudStorageFileSet;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import com.google.privacy.dlp.v2.StoredInfoTypeName;
import com.google.privacy.dlp.v2.UpdateStoredInfoTypeRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateStoredInfoType {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The path to file in GCS bucket that holds a collection of words and phrases to be searched by
    // the new infoType detector.
    String filePath = "gs://" + "your-bucket-name" + "/path/to/your/file.txt";
    // The path to the location in a GCS bucket to store the created dictionary.
    String outputPath = "your-cloud-storage-file-set";
    // The name of the stored InfoType which is to be updated.
    String infoTypeId = "your-stored-info-type-id";
    updateStoredInfoType(projectId, filePath, outputPath, infoTypeId);
  }

  // Update the stored info type rebuilding the Custom dictionary.
  public static void updateStoredInfoType(
      String projectId, String filePath, String outputPath, String infoTypeId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set path in Cloud Storage.
      CloudStoragePath cloudStoragePath = CloudStoragePath.newBuilder().setPath(outputPath).build();
      CloudStorageFileSet cloudStorageFileSet =
          CloudStorageFileSet.newBuilder().setUrl(filePath).build();

      // Configuration for a custom dictionary created from a data source of any size
      LargeCustomDictionaryConfig largeCustomDictionaryConfig =
          LargeCustomDictionaryConfig.newBuilder()
              .setOutputPath(cloudStoragePath)
              .setCloudStorageFileSet(cloudStorageFileSet)
              .build();

      // Set configuration for stored infoTypes.
      StoredInfoTypeConfig storedInfoTypeConfig =
          StoredInfoTypeConfig.newBuilder()
              .setLargeCustomDictionary(largeCustomDictionaryConfig)
              .build();

      // Set mask to control which fields get updated.
      // Refer https://protobuf.dev/reference/protobuf/google.protobuf/#field-mask for constructing the field mask paths.
      FieldMask fieldMask =
          FieldMask.newBuilder()
              .addPaths("large_custom_dictionary.cloud_storage_file_set.url")
              .build();

      // Construct the job creation request to be sent by the client.
      UpdateStoredInfoTypeRequest updateStoredInfoTypeRequest =
          UpdateStoredInfoTypeRequest.newBuilder()
              .setName(
                  StoredInfoTypeName.ofProjectStoredInfoTypeName(projectId, infoTypeId).toString())
              .setConfig(storedInfoTypeConfig)
              .setUpdateMask(fieldMask)
              .build();

      // Send the job creation request and process the response.
      StoredInfoType response = dlp.updateStoredInfoType(updateStoredInfoTypeRequest);

      // Print the results.
      System.out.println("Updated stored InfoType successfully: " + response.getName());
    }
  }
}
// [END dlp_update_stored_infotype]
