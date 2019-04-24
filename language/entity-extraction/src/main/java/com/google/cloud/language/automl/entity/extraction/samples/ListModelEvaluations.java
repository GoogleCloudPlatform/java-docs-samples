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

package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_list_model_evaluations]

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics.ConfidenceMetricsEntry;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

class ListModelEvaluations {

  // List Model evaluations for a given Model
  static void listModelEvaluations(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // String projectId = "YOUR_PROJECT_  ID";
    // String modelId = "YOUR_MODEL_ID";
    // String filter = "YOUR_FILTER_EXPRESSION";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Create list model evaluations request.
    ListModelEvaluationsRequest modelEvaluationsRequest =
        ListModelEvaluationsRequest.newBuilder()
            .setParent(modelFullId.toString())
            .setFilter(filter)
            .build();

    // List all the model evaluations in the model by applying filter.
    for (ModelEvaluation element :
        client.listModelEvaluations(modelEvaluationsRequest).iterateAll()) {
      // Display the model evaluations information.
      System.out.println(String.format("Model evaluation name: %s", element.getName()));
      System.out.println(
          String.format(
              "Model evaluation Id: %s",
              element.getName().split("/")[element.getName().split("/").length - 1]));
      System.out.println(
          String.format("Model evaluation annotation spec Id: %s", element.getAnnotationSpecId()));
      System.out.println(
          String.format("Model evaluation example count: %s", element.getEvaluatedExampleCount()));
      System.out.println(
          String.format("Model evaluation display name: %s", element.getDisplayName()));
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(element.getCreateTime().getSeconds() * 1000));
      System.out.println(String.format("Model evaluation create time: %s", createTime));

      System.out.println("Text extraction evaluation metrics:");
      TextExtractionEvaluationMetrics textExtractionMetrics =
          element.getTextExtractionEvaluationMetrics();
      List<ConfidenceMetricsEntry> confidenceMetricsEntries =
          textExtractionMetrics.getConfidenceMetricsEntriesList();

      System.out.println("\tConfidence metrics entries:");
      // Showing text extraction evaluation metrics
      for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
        System.out.println(
            String.format(
                "\t\tModel confidence threshold: %.2f ",
                confidenceMetricsEntry.getConfidenceThreshold()));
        System.out.println(
            String.format("\t\tModel recall: %.2f ", confidenceMetricsEntry.getRecall() * 100)
                + '%');
        System.out.println(
            String.format("\t\tModel precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
                + '%');
        System.out.println(
            String.format("\t\tModel f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
                + '%'
                + '\n');
      }
    }
  }
}
// [END automl_natural_language_entity_list_model_evaluations]
