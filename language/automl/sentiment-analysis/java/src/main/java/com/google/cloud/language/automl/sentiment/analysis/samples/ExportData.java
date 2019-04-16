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

package com.google.cloud.language.automl.sentiment.analysis.samples;

// [START automl_natural_language_sentiment_export_data]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.OutputConfig;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

class ExportData {

  static void exportData(String projectId, String computeRegion, String datasetId, String gcsUri)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String datasetId = "YOUR_DATASET_ID";
    // String gcsUri = "GCS_PATH_TO_DIRECTORY";

    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    // Set the output URI.
    GcsDestination gcsDestination = GcsDestination.newBuilder().setOutputUriPrefix(gcsUri).build();

    // Export the data to the output URI.
    OutputConfig outputConfig = OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
    System.out.println("Processing export...");

    // Export the data in the destination URI.
    Empty response = client.exportDataAsync(datasetFullId, outputConfig).get();
    System.out.println(String.format("Dataset exported. %s", response));
  }
}
// [END automl_natural_language_sentiment_export_data]
