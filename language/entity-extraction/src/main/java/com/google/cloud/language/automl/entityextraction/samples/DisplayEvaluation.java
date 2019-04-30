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

package com.google.cloud.language.automl.entityextraction.samples;

// [START automl_natural_language_entity_display_evaluation]

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelEvaluationName;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics;
import com.google.cloud.automl.v1beta1.TextExtractionEvaluationMetrics.ConfidenceMetricsEntry;
import java.io.IOException;
import java.util.List;

class DisplayEvaluation {

  // Display Model Evaluation
  static void displayEvaluation(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "us-central1";
    // String modelId = "YOUR_MODEL_ID";
    // String filter = "YOUR_FILTER_EXPRESSION";

    // Instantiates a client
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

      // List all the model evaluations in the model by applying filter.
      ListModelEvaluationsRequest modelEvaluationsrequest =
          ListModelEvaluationsRequest.newBuilder()
              .setParent(modelName.toString())
              .setFilter(filter)
              .build();

      // Iterate through the results.
      String modelEvaluationId = "";
      for (ModelEvaluation element :
          client.listModelEvaluations(modelEvaluationsrequest).iterateAll()) {
        // There is evaluation for each class in a model and for overall model.
        // Get only the evaluation of overall model.
        if (element.getAnnotationSpecId().isEmpty()) {
          modelEvaluationId = element.getName().split("/")[element.getName().split("/").length - 1];
        }
      }

      System.out.println("Model Evaluation ID:" + modelEvaluationId);

      // Resource name for the model evaluation.
      ModelEvaluationName modelEvaluationFullId =
          ModelEvaluationName.of(projectId, computeRegion, modelId, modelEvaluationId);

      // Get a model evaluation.
      ModelEvaluation modelEvaluation = client.getModelEvaluation(modelEvaluationFullId);

      TextExtractionEvaluationMetrics textExtractionMetrics =
          modelEvaluation.getTextExtractionEvaluationMetrics();
      List<ConfidenceMetricsEntry> confidenceMetricsEntries =
          textExtractionMetrics.getConfidenceMetricsEntriesList();

      // Showing model score based on threshold of 0.5
      for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
        if (confidenceMetricsEntry.getConfidenceThreshold() == 0.5) {
          System.out.println("Precision and recall are based on a score threshold of 0.5");
          System.out.println(
              String.format("Model precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
                  + '%');
          System.out.println(
              String.format("Model recall: %.2f ", confidenceMetricsEntry.getRecall() * 100) + '%');
          System.out.println(
              String.format("Model f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
                  + '%');
        }
      }
    }
  }
}
// [END automl_natural_language_entity_display_evaluation]
