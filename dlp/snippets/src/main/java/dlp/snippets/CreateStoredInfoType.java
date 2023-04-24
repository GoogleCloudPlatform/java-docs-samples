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
import com.google.privacy.dlp.v2.CloudStorageFileSet;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.CreateStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import java.io.IOException;

public class CreateStoredInfoType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    //The Google Cloud project id to use as a parent resource.
    String projectId = "bdp-2059-is-31084";
    // Specify the file in GCS bucket to be used as a term list.
    String gcsPath = "gs://" + "your-bucket-name" + "path/to/file.txt";
    // The path to the location in a GCS bucket to store the created dictionary.
    String outputPath = "gs://" + "your-bucket-name" + "path/to/directory";
    createStoredInfoType(projectId, gcsPath, outputPath);
  }

  public static void createStoredInfoType(String projectId, String gcsPath, String outputPath)
      throws IOException {
    try (DlpServiceClient dlp = DlpServiceClient.create()) {

      // Optionally set a display name and a description.
      String displayName = "Java Test";
      String description = "Dictionary of GitHub usernames used in commits";

      CloudStoragePath cloudStoragePath =
          CloudStoragePath.newBuilder()
              .setPath(outputPath)
              .build();

      CloudStorageFileSet cloudStorageFileSet = CloudStorageFileSet.newBuilder()
              .setUrl(gcsPath)
              .build();

      LargeCustomDictionaryConfig largeCustomDictionaryConfig =
          LargeCustomDictionaryConfig.newBuilder()
              .setOutputPath(cloudStoragePath)
              .setCloudStorageFileSet(cloudStorageFileSet)
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
              .setStoredInfoTypeId("test-new")
              .build();

      // Send the request and receive response from the service.
      StoredInfoType response = dlp.createStoredInfoType(createStoredInfoType);

      // Print the results.
      System.out.println("Created Stored InfoType: " + response);
    }
  }
}

// [END dlp_create_stored_infotype]
