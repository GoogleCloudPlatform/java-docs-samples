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

// [START automl_natural_language_sentiment_import_data]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.InputConfig;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class ImportData {

  // Import data from Google Cloud Storage into a dataset
  static void importData(String projectId, String computeRegion, String datasetId, String gcsPath)
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String datasetId = "YOUR_DATASET_ID";
    // String gcsPath = "GCS_PATH_TO_FILE";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Get multiple training data files to be imported from gcsSource.
    String[] inputUris = gcsPath.split(",");
    for (String inputUri : inputUris) {
      gcsSource.addInputUris(inputUri);
    }

    // Import data from the input URI
    InputConfig inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();

    OperationFuture<Empty, OperationMetadata> response = client
        .importDataAsync(datasetFullId, inputConfig);

    System.out
        .format("Import data operation name: %s \n", response.getInitialFuture().get().getName());
    System.out.println("Processing import...");
    //Cancel operation to prevent charges when testing
    client.getOperationsClient().cancelOperation(response.getInitialFuture().get().getName());
    client.close();
  }
}
// [END automl_natural_language_sentiment_import_data]
