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

// [START automl_natural_language_sentiment_list_models]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ListModelsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class ListModels {

  // List all models for a given project based on the filter expression
  static void listModels(String projectId, String computeRegion, String filter) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "us-central1";
    // String filter = "YOUR_FILTER_EXPRESSION";

    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Create list models request.
      ListModelsRequest listModelsRequest =
          ListModelsRequest.newBuilder()
              .setParent(projectLocation.toString())
              .setFilter(filter)
              .build();

      // List all the models available in the region by applying filter.
      System.out.println("List of models:");
      for (Model model : client.listModels(listModelsRequest).iterateAll()) {

        // Display the model information.
        System.out.println(String.format("\nModel name: %s", model.getName()));
        System.out.println(
            String.format(
                "Model Id: %s", model.getName().split("/")[model.getName().split("/").length - 1]));
        System.out.println(String.format("Model display name: %s", model.getDisplayName()));
        System.out.println(String.format("Dataset Id: %s", model.getDatasetId()));
        System.out.println(
            String.format("TextSentimentModelMetadata: %s", model.getTextSentimentModelMetadata()));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String createTime =
            dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
        System.out.println(String.format("Model create time: %s", createTime));
        System.out.println(String.format("Model deployment state: %s", model.getDeploymentState()));
      }
    }
  }
}
// [END automl_natural_language_sentiment_list_models]
