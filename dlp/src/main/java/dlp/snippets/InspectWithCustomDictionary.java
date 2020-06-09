/*
 * Copyright 2019 Google LLC
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

// [START dlp_inspect_custom_dictionary]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.CloudStorageOptions;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.CustomInfoType.DetectionRule;
import com.google.privacy.dlp.v2.CustomInfoType.DetectionRule.HotwordRule;
import com.google.privacy.dlp.v2.CustomInfoType.Dictionary;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectJobConfig;
import com.google.privacy.dlp.v2.Likelihood;
import com.google.privacy.dlp.v2.StorageConfig;
import com.google.privacy.dlp.v2.StorageConfig.TimespanConfig;
import java.io.IOException;

public class InspectWithCustomDictionary {
  public static void inspectWithCustomDictionary() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String storageGcsPath = "gs://" + "your-bucket-name" + "path/to/file.txt";
    String dictionaryGcsPath = "gs://" + "your-bucket-name" + "path/to/dictionary.txt";
    inspectWithCustomDictionary(projectId, dictionaryGcsPath, storageGcsPath);
  }

  // Creates a DLP Job
  public static void inspectWithCustomDictionary(
      String projectId, String dictionaryGcsPath, String storageGcsPath) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // Set autoPopulateTimespan to true to scan only new content
      boolean autoPopulateTimespan = true;
      TimespanConfig timespanConfig =
          TimespanConfig.newBuilder()
              .setEnableAutoPopulationOfTimespanConfig(autoPopulateTimespan)
              .build();

      // Specify the GCS file to be inspected.
      CloudStorageOptions cloudStorageOptions =
          CloudStorageOptions.newBuilder()
              .setFileSet(CloudStorageOptions.FileSet.newBuilder().setUrl(storageGcsPath))
              .build();
      StorageConfig storageConfig =
          StorageConfig.newBuilder()
              .setCloudStorageOptions(cloudStorageOptions)
              .setTimespanConfig(timespanConfig)
              .build();

      // Build the type of information, specify the name of the information type.
      InfoType infoType = InfoType.newBuilder().setName("CUSTOM_DICTIONARY").build();

      // The minimum likelihood required before returning a match:
      // See: https://cloud.google.com/dlp/docs/likelihood
      Likelihood minLikelihood = Likelihood.UNLIKELY;

      // Specify detection rulse for the custom dictionary detector.
      DetectionRule detectionRule =
          DetectionRule.newBuilder().setHotwordRule(HotwordRule.getDefaultInstance()).build();

      // Build dictionary fo the custom dictionary detector.
      Dictionary dictionary =
          Dictionary.newBuilder()
              .setCloudStoragePath(CloudStoragePath.newBuilder().setPath(dictionaryGcsPath).build())
              .build();

      // Build custom dictionary detector from cloud storage path
      CustomInfoType customDictionaryDetector =
          CustomInfoType.newBuilder()
              .setInfoType(infoType)
              .setLikelihood(minLikelihood)
              .addDetectionRules(detectionRule)
              .setDictionary(dictionary)
              .build();

      // Construct the configuration for the Inspect request.
      InspectConfig inspectConfig =
          InspectConfig.newBuilder()
              .addCustomInfoTypes(customDictionaryDetector)
              .setIncludeQuote(true)
              .build();

      // Configure the long running job we want the service to perform.
      InspectJobConfig inspectJobConfig =
          InspectJobConfig.newBuilder()
              .setStorageConfig(storageConfig)
              .setInspectConfig(inspectConfig)
              .build();

      // Create the request for the job configured above.
      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setInspectJob(inspectJobConfig)
              .build();

      // Send the job creation request and process the response.
      DlpJob createdDlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);
      System.out.println("Job created: " + createdDlpJob.getName());
    }
  }
}
// [END dlp_inspect_custom_dictionary]
