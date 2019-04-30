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

package com.google.cloud.language.automl.sentimentanalysis.samples;

// [START automl_natural_language_sentiment_create_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.TextSentimentModelMetadata;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

class CreateModel {

  // Create a model
  static void createModel(
      String projectId, String computeRegion, String datasetId, String modelName)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "us-central1";
    // String datasetId = "YOUR_DATASET_ID";
    // String modelName = "YOUR_MODEL_NAME";

    // Instantiates a client
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Set model meta data
      TextSentimentModelMetadata textSentimentModelMetadata =
          TextSentimentModelMetadata.newBuilder().build();

      // Set model name, dataset Id and model metadata.
      Model myModel =
          Model.newBuilder()
              .setDisplayName(modelName)
              .setDatasetId(datasetId)
              .setTextSentimentModelMetadata(textSentimentModelMetadata)
              .build();

      // Create a model with the model metadata in the region.
      OperationFuture<Model, OperationMetadata> response =
          client.createModelAsync(projectLocation, myModel);

      System.out.println(
          String
              .format("Training operation name: %s", response.getInitialFuture().get().getName()));
      System.out.println("Training started...");
    }
  }
}
// [END automl_natural_language_sentiment_create_model]
