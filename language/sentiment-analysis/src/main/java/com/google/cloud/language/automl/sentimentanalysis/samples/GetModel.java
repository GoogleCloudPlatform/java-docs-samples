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

// [START automl_natural_language_sentiment_get_model]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.ModelName;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class GetModel {

  // Get a given model
  static void getModel(String projectId, String computeRegion, String modelId) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String datasetId = "YOUR_DATASET_ID";
    // String ModelId = "YOUR_MODEL_ID";

    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

      // Get complete detail of the model.
      Model model = client.getModel(modelName);
      client.close();
      // Display the model information.
      System.out.println(String.format("Model name: %s", model.getName()));
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
// [END automl_natural_language_sentiment_get_model]
