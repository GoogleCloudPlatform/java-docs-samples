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

// [START automl_natural_language_sentiment_get_model_evaluation]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix.Row;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelEvaluationName;
import com.google.cloud.automl.v1beta1.TextSentimentProto.TextSentimentEvaluationMetrics;
import com.google.protobuf.ProtocolStringList;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

class GetModelEvaluation {

  // Get a given Model Evaluation
  static void getModelEvaluation(
      String projectId, String computeRegion, String modelId, String modelEvaluationId)
      throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String datasetId = "YOUR_DATASET_ID";
    // String ModelId = "YOUR_MODEL_ID";
    // String modelEvaluationId = "YOUR_MODEL_EVALUATION_ID";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model evaluation.
    ModelEvaluationName modelEvaluationFullId =
        ModelEvaluationName.of(projectId, computeRegion, modelId, modelEvaluationId);

    // Get complete detail of the model evaluation.
    ModelEvaluation response = client.getModelEvaluation(modelEvaluationFullId);

    // Display the model evaluations information.
    System.out.println(String.format("Model evaluation name: %s", response.getName()));
    System.out.println(
        String.format(
            "Model evaluation Id: %s",
            response.getName().split("/")[response.getName().split("/").length - 1]));
    System.out.println(
        String.format("Model evaluation annotation spec Id: %s", response.getAnnotationSpecId()));
    System.out.println(
        String.format("Model evaluation example count: %s", response.getEvaluatedExampleCount()));
    System.out.println(
        String.format("Model evaluation display name: %s", response.getDisplayName()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(response.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Model evaluation create time: %s", createTime));

    System.out.println("Text sentiment evaluation metrics:");
    TextSentimentEvaluationMetrics textSentimentMetrics =
        response.getTextSentimentEvaluationMetrics();

    // Showing text sentiment evaluation metrics
    System.out.println(
        String.format("\tModel precision: %.2f ", textSentimentMetrics.getPrecision() * 100) + '%');
    System.out.println(
        String.format("\tModel recall: %.2f ", textSentimentMetrics.getRecall() * 100) + '%');
    System.out.println(
        String.format("\tModel f1 score: %.2f ", textSentimentMetrics.getF1Score() * 100) + '%');

    if (response.getAnnotationSpecId().isEmpty()) {
      System.out.println(
          String.format(
                  "\tModel mean absolute error: %.2f ",
                  textSentimentMetrics.getMeanAbsoluteError() * 100)
              + '%');
      System.out.println(
          String.format(
                  "\tModel mean squared error: %.2f ",
                  textSentimentMetrics.getMeanSquaredError() * 100)
              + '%');
      System.out.println(
          String.format("\tModel linear kappa: %.2f ", textSentimentMetrics.getLinearKappa() * 100)
              + '%');
      System.out.println(
          String.format(
                  "\tModel quadratic kappa: %.2f ", textSentimentMetrics.getQuadraticKappa() * 100)
              + '%');

      ConfusionMatrix confusionMatrix = textSentimentMetrics.getConfusionMatrix();

      ProtocolStringList annotationSpecIdList = confusionMatrix.getAnnotationSpecIdList();
      System.out.println("\tModel confusion matrix:");
      for (String annotationSpecId : annotationSpecIdList) {
        System.out.println(String.format("\t\tAnnotation spec Id: " + annotationSpecId));
      }
      List<Row> rowList = confusionMatrix.getRowList();

      for (Row row : rowList) {
        System.out.println("\t\tRow:");
        List<Integer> exampleCountList = row.getExampleCountList();
        for (Integer exampleCount : exampleCountList) {
          System.out.println(String.format("\t\t\tExample count: " + exampleCount));
        }
      }
      annotationSpecIdList = textSentimentMetrics.getAnnotationSpecIdList();
      for (String annotationSpecId : annotationSpecIdList) {
        System.out.println(String.format("\tAnnotation spec Id: " + annotationSpecId));
      }
    }
    client.close();
  }
}
// [END automl_natural_language_sentiment_get_model_evaluation]
