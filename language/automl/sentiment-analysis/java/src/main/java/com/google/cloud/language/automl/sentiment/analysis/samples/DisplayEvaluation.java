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

// [START automl_natural_language_sentiment_display_evaluation]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix.Row;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelEvaluationName;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.TextSentimentProto.TextSentimentEvaluationMetrics;
import com.google.protobuf.ProtocolStringList;
import java.io.IOException;
import java.util.List;

public class DisplayEvaluation {

  // Display Model Evaluation
  public static void displayEvaluation(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String modelId = "YOUR_MODEL_ID";
    // String filter = "YOUR_FILTER_EXPRESSION";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // List all the model evaluations in the model by applying.
    ListModelEvaluationsRequest modelEvaluationsrequest =
        ListModelEvaluationsRequest.newBuilder()
            .setParent(modelFullId.toString())
            .setFilter(filter)
            .build();

    // Iterate through the results.
    String modelEvaluationId = "";
    for (ModelEvaluation element :
        client.listModelEvaluations(modelEvaluationsrequest).iterateAll()) {
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

    TextSentimentEvaluationMetrics textSentimentMetrics =
        modelEvaluation.getTextSentimentEvaluationMetrics();

    // Showing text sentiment evaluation metrics
    System.out.println(
        String.format("Model precision: %.2f ", textSentimentMetrics.getPrecision() * 100) + '%');
    System.out.println(
        String.format("Model recall: %.2f ", textSentimentMetrics.getRecall() * 100) + '%');
    System.out.println(
        String.format("Model f1 score: %.2f ", textSentimentMetrics.getF1Score() * 100) + '%');
    System.out.println(
        String.format(
                "Model mean absolute error: %.2f ",
                textSentimentMetrics.getMeanAbsoluteError() * 100)
            + '%');
    System.out.println(
        String.format(
                "Model mean squared error: %.2f ", textSentimentMetrics.getMeanSquaredError() * 100)
            + '%');
    System.out.println(
        String.format("Model linear kappa: %.2f ", textSentimentMetrics.getLinearKappa() * 100)
            + '%');
    System.out.println(
        String.format(
                "Model quadratic kappa: %.2f ", textSentimentMetrics.getQuadraticKappa() * 100)
            + '%');

    ConfusionMatrix confusionMatrix = textSentimentMetrics.getConfusionMatrix();

    ProtocolStringList annotationSpecIdList = confusionMatrix.getAnnotationSpecIdList();
    System.out.println("Model confusion matrix:");
    for (String annotationSpecId : annotationSpecIdList) {
      System.out.println(String.format("\tAnnotation spec Id: " + annotationSpecId));
    }
    List<Row> rowList = confusionMatrix.getRowList();

    for (Row row : rowList) {
      System.out.println("\tRow:");
      List<Integer> exampleCountList = row.getExampleCountList();
      for (Integer exampleCount : exampleCountList) {
        System.out.println(String.format("\t\tExample count: " + exampleCount));
      }
    }
    annotationSpecIdList = textSentimentMetrics.getAnnotationSpecIdList();
    for (String annotationSpecId : annotationSpecIdList) {
      System.out.println(String.format("Annotation spec Id: " + annotationSpecId));
    }
  }
}
// [END automl_natural_language_sentiment_display_evaluation]
