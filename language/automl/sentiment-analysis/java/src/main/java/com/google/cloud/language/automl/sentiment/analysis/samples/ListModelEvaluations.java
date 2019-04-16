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

// [START automl_natural_language_sentiment_list_model_evaluations]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfusionMatrix.Row;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.TextSentimentProto.TextSentimentEvaluationMetrics;
import com.google.protobuf.ProtocolStringList;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

class ListModelEvaluations {

  static void listModelEvaluations(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
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

      System.out.println("Text sentiment evaluation metrics:");
      TextSentimentEvaluationMetrics textSentimentMetrics =
          element.getTextSentimentEvaluationMetrics();

      // Showing text sentiment evaluation metrics
      System.out.println(
          String.format("\tModel precision: %.2f ", textSentimentMetrics.getPrecision() * 100)
              + '%');
      System.out.println(
          String.format("\tModel recall: %.2f ", textSentimentMetrics.getRecall() * 100) + '%');
      System.out.println(
          String.format("\tModel f1 score: %.2f ", textSentimentMetrics.getF1Score() * 100) + '%');
      if (element.getAnnotationSpecId().isEmpty()) {
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
            String.format(
                    "\tModel linear kappa: %.2f ", textSentimentMetrics.getLinearKappa() * 100)
                + '%');
        System.out.println(
            String.format(
                    "\tModel quadratic kappa: %.2f ",
                    textSentimentMetrics.getQuadraticKappa() * 100)
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
      System.out.print("\n");
    }
  }
}
// [END automl_natural_language_sentiment_list_model_evaluations]
