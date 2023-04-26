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

// [START dlp_update_stored_infotype]

package dlp.snippets;

// [Start dlp_update_stored_infotype]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.CloudStorageFileSet;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import com.google.privacy.dlp.v2.UpdateStoredInfoTypeRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateStoredInfoType {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The name of the file in the bucket.
    String gcsUri = "gs://" + "your-bucket-name" + "/path/to/your/file.txt";
    // The url of the file to be created in the bucket.
    String fileSetUrl = "your-cloud-storage-file-set";
    // The name of the stored info-type which is to be updated.
    String infoTypeId = "your-stored-info-type-id";
    updateStoredInfoType(projectId, gcsUri, fileSetUrl, infoTypeId);
  }

  // Update the stored info type rebuilding the Custom dictionary.
  public static void updateStoredInfoType(
      String projectId, String gcsUri, String fileSetUrl, String infoTypeId) throws IOException {
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set path in Cloud Storage.
      CloudStoragePath cloudStoragePath = CloudStoragePath.newBuilder().setPath(gcsUri).build();
      CloudStorageFileSet cloudStorageFileSet =
          CloudStorageFileSet.newBuilder().setUrl(fileSetUrl).build();

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
      FieldMask fieldMask =
          FieldMask.newBuilder()
              .addPaths("large_custom_dictionary.cloud_storage_file_set.url")
              .build();

      // Construct the job creation request to be sent by the client.
      UpdateStoredInfoTypeRequest updateStoredInfoTypeRequest =
          UpdateStoredInfoTypeRequest.newBuilder()
              .setName("projects/" + projectId + "/storedInfoTypes/" + infoTypeId)
              .setConfig(storedInfoTypeConfig)
              .setUpdateMask(fieldMask)
              .build();

      // Send the job creation request and process the response.
      StoredInfoType response = dlp.updateStoredInfoType(updateStoredInfoTypeRequest);

      // Print the results.
      System.out.println("InfoType stored successfully at " + response.getName());
    }
  }
}
// [End dlp_update_stored_infotype]
