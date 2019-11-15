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

package com.google.cloud.automl.tables.samples;

// Imports the Google Cloud client library
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.BigQueryDestination;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics;
import com.google.cloud.automl.v1beta1.ClassificationProto.ClassificationEvaluationMetrics.ConfidenceMetricsEntry;
import com.google.cloud.automl.v1beta1.ColumnSpec;
import com.google.cloud.automl.v1beta1.ColumnSpecName;
import com.google.cloud.automl.v1beta1.CreateModelRequest;
import com.google.cloud.automl.v1beta1.DeployModelRequest;
import com.google.cloud.automl.v1beta1.ExportEvaluatedExamplesOutputConfig;
import com.google.cloud.automl.v1beta1.ExportEvaluatedExamplesRequest;
import com.google.cloud.automl.v1beta1.GetColumnSpecRequest;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ListModelsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelEvaluationName;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.RegressionProto.RegressionEvaluationMetrics;
import com.google.cloud.automl.v1beta1.TablesModelColumnInfo;
import com.google.cloud.automl.v1beta1.TablesModelMetadata;
import com.google.cloud.automl.v1beta1.UndeployModelRequest;
import com.google.longrunning.ListOperationsRequest;
import com.google.longrunning.Operation;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModelApi {

  // [START automl_tables_create_model]
  /**
   * Demonstrates using the AutoML client to create a model.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset to which model is created.
   * @param tableId the Id of the table.
   * @param columnId the Id of the column.
   * @param modelName the name of the model.
   * @param trainBudget the integer amount for maximum cost of model.
   */
  public static void createModel(
      String projectId,
      String computeRegion,
      String datasetId,
      String tableId,
      String columnId,
      String modelName,
      String trainBudget)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Get the complete path of the column.
      ColumnSpecName columnSpecId =
          ColumnSpecName.of(projectId, computeRegion, datasetId, tableId, columnId);

      // Build the get column spec request.
      GetColumnSpecRequest columnSpec =
          GetColumnSpecRequest.newBuilder().setName(columnSpecId.toString()).build();

      // Build the get column spec.
      ColumnSpec targetColumnSpec =
          ColumnSpec.newBuilder().setName(columnSpec.getName().toString()).build();

      // Set model meta data.
      TablesModelMetadata tablesModelMetadata =
          TablesModelMetadata.newBuilder()
              .setTargetColumnSpec(targetColumnSpec)
              .setTrainBudgetMilliNodeHours(Long.valueOf(trainBudget))
              .build();

      // Set model name, dataset and metadata.
      Model myModel =
          Model.newBuilder()
              .setDatasetId(datasetId)
              .setDisplayName(modelName)
              .setTablesModelMetadata(tablesModelMetadata)
              .build();

      // Build the Create model request.
      CreateModelRequest createModelRequestrequest =
          CreateModelRequest.newBuilder()
              .setParent(projectLocation.toString())
              .setModel(myModel)
              .build();

      // Create a model with the model metadata in the region.
      OperationFuture<Model, OperationMetadata> response =
          client.createModelAsync(createModelRequestrequest);

      System.out.format(
          "Training operation name: %s\n", response.getInitialFuture().get().getName());
      System.out.println("Training started...");
    }
  }
  // [END automl_tables_create_model]

  // [START automl_tables_get_operation_status]
  /**
   * Demonstrates using the AutoML client to get operation status.
   *
   * @param operationFullId the complete name of a operation. For example, the name of your
   *     operation is projects/[projectId]/locations/us-central1/operations/[operationId].
   */
  public static void getOperationStatus(String operationFullId) throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the latest state of a long-running operation.
      Operation response = client.getOperationsClient().getOperation(operationFullId);

      // Display operation details.
      System.out.println("Operation details:");
      System.out.format("\tName: %s\n", response.getName());
      System.out.println("\tMetadata:\n");
      System.out.format("\t\tType Url: %s\n", response.getMetadata().getTypeUrl());
      System.out.format(
          "\t\tValue: %s\n", response.getMetadata().getValue().toStringUtf8().replace("\n", ""));
      System.out.format("\tDone: %s\n", response.getDone());
      if (response.hasResponse()) {
        System.out.println("\tResponse:");
        System.out.format("\t\tType Url: %s\n", response.getResponse().getTypeUrl());
        System.out.format(
            "\t\tValue: %s\n", response.getResponse().getValue().toStringUtf8().replace("\n", ""));
      }
      if (response.hasError()) {
        System.out.println("\tResponse:");
        System.out.format("\t\tError code: %s\n", response.getError().getCode());
        System.out.format("\t\tError message: %s\n", response.getError().getMessage());
      }
    }
  }
  // [END automl_tables_get_operation_status]

  // [START automl_tables_list_operations_status]
  /**
   * Demonstrates using the AutoML client to list the operations status.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param filter the filter expression.
   */
  public static void listOperationsStatus(String projectId, String computeRegion, String filter)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Create list operations request.
      ListOperationsRequest listrequest =
          ListOperationsRequest.newBuilder()
              .setName(projectLocation.toString())
              .setFilter(filter)
              .build();

      // List all the operations names available in the region by applying filter.
      for (Operation operation :
          client.getOperationsClient().listOperations(listrequest).iterateAll()) {
        // Display operation details.
        System.out.println("Operation details:");
        System.out.format("\tName: %s\n", operation.getName());
        System.out.println("\tMetadata:");
        System.out.format("\t\tType Url: %s\n", operation.getMetadata().getTypeUrl());
        System.out.format(
            "\t\tValue: %s\n", operation.getMetadata().getValue().toStringUtf8().replace("\n", ""));

        System.out.format("\tDone: %s\n", operation.getDone());
        if (operation.hasResponse()) {
          System.out.println("\tResponse:");
          System.out.format("\t\tType Url: %s\n", operation.getResponse().getTypeUrl());
          System.out.format(
              "\t\tValue: %s\n",
              operation.getResponse().getValue().toStringUtf8().replace("\n", ""));
        }
        if (operation.hasError()) {
          System.out.println("\tResponse:");
          System.out.format("\t\tError code: %s\n", operation.getError().getCode());
          System.out.format("\t\tError message: %s\n", operation.getError().getMessage());
        }
      }
    }
  }
  // [END automl_tables_list_operations_status]

  // [START automl_tables_list_models]
  /**
   * Demonstrates using the AutoML client to list all models.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param filter the filter expression.
   */
  public static void listModels(String projectId, String computeRegion, String filter)
      throws IOException {
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
        System.out.format("\nModel name: %s\n", model.getName());
        System.out.format(
            "Model Id: %s\n", model.getName().split("/")[model.getName().split("/").length - 1]);
        System.out.format("Model display name: %s\n", model.getDisplayName());
        System.out.format("Dataset Id: %s\n", model.getDatasetId());
        System.out.println("Tables Model Metadata: ");
        System.out.format(
            "\tTraining budget: %s\n",
            model.getTablesModelMetadata().getTrainBudgetMilliNodeHours());
        System.out.format(
            "\tTraining cost: %s\n", model.getTablesModelMetadata().getTrainCostMilliNodeHours());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String createTime =
            dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
        System.out.format("Model create time: %s\n", createTime);

        System.out.format("Model deployment state: %s\n", model.getDeploymentState());
      }
    }
  }
  // [END automl_tables_list_models]

  // [START automl_tables_get_model]
  /**
   * Demonstrates using the AutoML client to get model details.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   */
  public static void getModel(String projectId, String computeRegion, String modelId)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

      // Get complete detail of the model.
      Model model = client.getModel(modelFullId);

      // Display the model information.
      System.out.format("Model name: %s\n", model.getName());
      System.out.format(
          "Model Id: %s\n", model.getName().split("/")[model.getName().split("/").length - 1]);
      System.out.format("Model display name: %s\n", model.getDisplayName());
      System.out.format("Dataset Id: %s\n", model.getDatasetId());
      System.out.println("Tables Model Metadata: ");
      System.out.format(
          "\tTraining budget: %s\n", model.getTablesModelMetadata().getTrainBudgetMilliNodeHours());
      System.out.format(
          "\tTraining cost: %s\n", model.getTablesModelMetadata().getTrainBudgetMilliNodeHours());

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
      System.out.format("Model create time: %s\n", createTime);

      System.out.format("Model deployment state: %s\n", model.getDeploymentState());

      // Get features of top importance
      for (TablesModelColumnInfo info :
          model.getTablesModelMetadata().getTablesModelColumnInfoList()) {
        System.out.format(
            "Column: %s - Importance: %.2f\n",
            info.getColumnDisplayName(), info.getFeatureImportance());
      }
    }
  }
  // [END automl_tables_get_model]

  // [START automl_tables_export_evaluated_examples]
  /**
   * Demonstrates using the AutoML client to export evaluated examples.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @param bigQueryUri the Destination URI (BigQuery)
   */
  public static void exportEvaluatedExamples(
      String projectId, String computeRegion, String modelId, String bigQueryUri)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

      // Set the Output URI.
      BigQueryDestination.Builder bigQueryDestination = BigQueryDestination.newBuilder();
      bigQueryDestination.setOutputUri(bigQueryUri);

      // Set the export evaluated examples output configuration.
      ExportEvaluatedExamplesOutputConfig outputConfig =
          ExportEvaluatedExamplesOutputConfig.newBuilder()
              .setBigqueryDestination(bigQueryDestination)
              .build();

      // Create export evaluated examples request.
      ExportEvaluatedExamplesRequest exportEvaluatedExamplesRequest =
          ExportEvaluatedExamplesRequest.newBuilder()
              .setName(modelFullId.toString())
              .setOutputConfig(outputConfig)
              .build();

      // Get the latest state of a long-running operation.
      OperationFuture<Empty, OperationMetadata> response =
          client.exportEvaluatedExamplesAsync(exportEvaluatedExamplesRequest);

      System.out.format("Operation name: %s\n", response);
    }
  }
  // [END automl_tables_export_evaluated_examples]

  // [START automl_tables_list_model_evaluations]
  /**
   * Demonstrates using the AutoML client to list model evaluations.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @param filter the Filter expression.
   */
  public static void listModelEvaluations(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

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
        System.out.format("Model evaluation name: %s\n", element.getName());
        System.out.format(
            "Model evaluation Id: %s\n",
            element.getName().split("/")[element.getName().split("/").length - 1]);
        System.out.format(
            "Model evaluation annotation spec Id: %s\n", element.getAnnotationSpecId());
        System.out.format(
            "Model evaluation example count: %s\n", element.getEvaluatedExampleCount());
        System.out.format("Model evaluation display name: %s\n", element.getDisplayName());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String createTime =
            dateFormat.format(new java.util.Date(element.getCreateTime().getSeconds() * 1000));
        System.out.format("Model evaluation create time: %s\n", createTime);

        int regressionLength = element.getRegressionEvaluationMetrics().toString().length();
        int classificationLength = element.getClassificationEvaluationMetrics().toString().length();

        if (classificationLength > 0) {
          ClassificationEvaluationMetrics classificationMetrics =
              element.getClassificationEvaluationMetrics();
          // Display tables classification model information.
          System.out.println("Table classification evaluation metrics:");
          System.out.format("\tModel au_prc: %f\n", classificationMetrics.getAuPrc());
          System.out.format("\tModel au_roc: %f\n", classificationMetrics.getAuRoc());
          System.out.format("\tModel log loss: %f\n", classificationMetrics.getLogLoss());

          List<ConfidenceMetricsEntry> confidenceMetricsEntries =
              classificationMetrics.getConfidenceMetricsEntryList();
          if (!confidenceMetricsEntries.isEmpty()) {
            System.out.println("\tConfidence metrics entries:");
            // Showing table classification evaluation metrics.
            for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
              System.out.format(
                  "\t\tModel confidence threshold: %.2f\n",
                  confidenceMetricsEntry.getConfidenceThreshold());
              System.out.format(
                  "\t\tModel precision: %.2f%%\n", confidenceMetricsEntry.getPrecision() * 100);
              System.out.format(
                  "\t\tModel recall: %.2f%%\n", confidenceMetricsEntry.getRecall() * 100);
              System.out.format(
                  "\t\tModel f1 score: %.2f%%\n", confidenceMetricsEntry.getF1Score() * 100);
              System.out.format(
                  "\t\tModel precision@1: %.2f%%\n",
                  confidenceMetricsEntry.getPrecisionAt1() * 100);
              System.out.format(
                  "\t\tModel recall@1: %.2f%%\n", confidenceMetricsEntry.getRecallAt1() * 100);
              System.out.format(
                  "\t\tModel f1 score@1: %.2f %%\n", confidenceMetricsEntry.getF1ScoreAt1() * 100);
              System.out.format(
                  "\t\tModel false positive rate: %.2f\n",
                  confidenceMetricsEntry.getFalsePositiveRate());
              System.out.format(
                  "\t\tModel true positive count: %s\n",
                  confidenceMetricsEntry.getTruePositiveCount());
              System.out.format(
                  "\t\tModel false positive count: %s\n",
                  confidenceMetricsEntry.getFalsePositiveCount());
              System.out.format(
                  "\t\tModel true negative count: %s\n",
                  confidenceMetricsEntry.getTrueNegativeCount());
              System.out.format(
                  "\t\tModel false negative count: %s\n",
                  confidenceMetricsEntry.getFalseNegativeCount());
              System.out.format(
                  "\t\tModel position threshold: %s\n",
                  confidenceMetricsEntry.getPositionThreshold());
            }
          }
        } else if (regressionLength > 0) {
          RegressionEvaluationMetrics regressionMetrics = element.getRegressionEvaluationMetrics();
          System.out.println("Table regression evaluation metrics:");
          // Showing tables regression evaluation metrics
          System.out.format(
              "\tModel root mean squared error: %.2f%%\n",
              regressionMetrics.getRootMeanSquaredError());
          System.out.format(
              "\tModel mean absolute error: %.2f%%\n", regressionMetrics.getMeanAbsoluteError());
          System.out.format(
              "\tModel mean absolute percentage error: %.2f%%\n",
              regressionMetrics.getMeanAbsolutePercentageError());
          System.out.format("\tModel rsquared: %.2f%%\n", regressionMetrics.getRSquared());
        }
      }
    }
  }
  // [END automl_tables_list_model_evaluations]

  // [START automl_tables_get_model_evaluation]
  /**
   * Demonstrates using the AutoML client to get model evaluations.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @param modelEvaluationId the Id of your model evaluation.
   */
  public static void getModelEvaluation(
      String projectId, String computeRegion, String modelId, String modelEvaluationId)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model evaluation.
      ModelEvaluationName modelEvaluationFullId =
          ModelEvaluationName.of(projectId, computeRegion, modelId, modelEvaluationId);

      // Get complete detail of the model evaluation.
      ModelEvaluation response = client.getModelEvaluation(modelEvaluationFullId);

      // Display the model evaluations information.
      System.out.format("Model evaluation name: %s\n", response.getName());
      System.out.format(
          "Model evaluation Id: %s\n",
          response.getName().split("/")[response.getName().split("/").length - 1]);
      System.out.format(
          "Model evaluation annotation spec Id: %s\n", response.getAnnotationSpecId());
      System.out.format(
          "Model evaluation example count: %s\n", response.getEvaluatedExampleCount());
      System.out.format("Model evaluation display name: %s\n", response.getDisplayName());

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(response.getCreateTime().getSeconds() * 1000));
      System.out.format("Model evaluation create time: %s\n", createTime);

      int regressionLength = response.getRegressionEvaluationMetrics().toString().length();
      int classificationLength = response.getClassificationEvaluationMetrics().toString().length();

      if (classificationLength > 0) {
        ClassificationEvaluationMetrics classificationMetrics =
            response.getClassificationEvaluationMetrics();
        List<ConfidenceMetricsEntry> confidenceMetricsEntries =
            classificationMetrics.getConfidenceMetricsEntryList();
        if (!confidenceMetricsEntries.isEmpty()) {
          System.out.println("Table classification evaluation metrics:");
          System.out.format("\tModel au_prc: %f\n", classificationMetrics.getAuPrc());
          System.out.format("\tModel au_roc: %f\n", classificationMetrics.getAuRoc());
          System.out.format("\tModel log loss: %f\n", classificationMetrics.getLogLoss());
          System.out.println("\tConfidence metrics entries:");
          // Showing tables classification evaluation metrics.
          for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
            System.out.format(
                "\t\tModel confidence threshold: %.2f \n",
                confidenceMetricsEntry.getConfidenceThreshold());
            System.out.format(
                "\t\tModel precision: %.2f%%\n", confidenceMetricsEntry.getPrecision() * 100);
            System.out.format(
                "\t\tModel recall: %.2f%%\n", confidenceMetricsEntry.getRecall() * 100);
            System.out.format(
                "\t\tModel f1 score: %.2f%%\n", confidenceMetricsEntry.getF1Score() * 100);
            System.out.format(
                "\t\tModel precision@1: %.2f%%\n", confidenceMetricsEntry.getPrecisionAt1() * 100);
            System.out.format(
                "\t\tModel recall@1: %.2f%%\n", confidenceMetricsEntry.getRecallAt1() * 100);
            System.out.format(
                "\t\tModel f1 score@1: %.2f%%\n", confidenceMetricsEntry.getF1ScoreAt1() * 100);
            System.out.format(
                "\t\tModel false positive rate: %.2f\n",
                confidenceMetricsEntry.getFalsePositiveRate());
            System.out.format(
                "\t\tModel true positive count: %s\n",
                confidenceMetricsEntry.getTruePositiveCount());
            System.out.format(
                "\t\tModel false positive count: %s\n",
                confidenceMetricsEntry.getFalsePositiveCount());
            System.out.format(
                "\t\tModel true negative count: %s\n",
                confidenceMetricsEntry.getTrueNegativeCount());
            System.out.format(
                "\t\tModel false negative count: %s\n",
                confidenceMetricsEntry.getFalseNegativeCount());
            System.out.format(
                "\t\tModel position threshold: %s\n",
                confidenceMetricsEntry.getPositionThreshold());
          }
        }
      } else if (regressionLength > 0) {
        RegressionEvaluationMetrics regressionMetrics = response.getRegressionEvaluationMetrics();
        System.out.println("Table regression evaluation metrics:");
        // Showing tables regression evaluation metrics.
        System.out.format(
            "\tModel root mean squared error: %.2f%%\n",
            regressionMetrics.getRootMeanSquaredError());
        System.out.format(
            "\tModel mean absolute error: %.2f%%\n", regressionMetrics.getMeanAbsoluteError());
        System.out.format(
            "\tModel mean absolute percentage error: %.2f%%\n",
            regressionMetrics.getMeanAbsolutePercentageError());
        System.out.format("\tModel rsquared: %.2f%%\n", regressionMetrics.getRSquared());
      }
    }
  }
  // [END automl_tables_get_model_evaluation]

  // [START automl_tables_display_evaluation]
  /**
   * Demonstrates using the AutoML client to display model evaluation.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @param filter the Filter expression.
   */
  public static void displayEvaluation(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

      // List all the model evaluations in the model by applying filter.
      ListModelEvaluationsRequest modelEvaluationsrequest =
          ListModelEvaluationsRequest.newBuilder()
              .setParent(modelFullId.toString())
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

      int regressionLength = modelEvaluation.getRegressionEvaluationMetrics().toString().length();
      int classificationLength =
          modelEvaluation.getClassificationEvaluationMetrics().toString().length();

      if (classificationLength > 0) {
        ClassificationEvaluationMetrics classificationMetrics =
            modelEvaluation.getClassificationEvaluationMetrics();
        List<ConfidenceMetricsEntry> confidenceMetricsEntries =
            classificationMetrics.getConfidenceMetricsEntryList();
        if (!confidenceMetricsEntries.isEmpty()) {
          // Showing tables classification evaluation metrics.
          for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
            if (confidenceMetricsEntry.getConfidenceThreshold() == 0.5) {
              System.out.println("Precision and recall are based on a score threshold of 0.5");
              System.out.format(
                  "Model precision: %.2f%%\n", confidenceMetricsEntry.getPrecision() * 100);
              System.out.format("Model recall: %.2f%%\n", confidenceMetricsEntry.getRecall() * 100);
              System.out.format(
                  "Model f1 score: %.2f%%\n", confidenceMetricsEntry.getF1Score() * 100);
              System.out.format(
                  "Model precision@1: %.2f%%\n", confidenceMetricsEntry.getPrecisionAt1() * 100);
              System.out.format(
                  "Model recall@1: %.2f%%\n", confidenceMetricsEntry.getRecallAt1() * 100);
              System.out.format(
                  "Model f1 score@1: %.2f%%\n", confidenceMetricsEntry.getF1ScoreAt1() * 100);
            }
          }
        }
      } else if (regressionLength > 0) {
        RegressionEvaluationMetrics regressionMetrics =
            modelEvaluation.getRegressionEvaluationMetrics();
        System.out.println("Table regression evaluation metrics:");
        // Showing tables regression evaluation metrics.
        System.out.format(
            "Model root mean squared error: %.2f%%\n", regressionMetrics.getRootMeanSquaredError());
        System.out.format(
            "Model mean absolute error: %.2f%%\n", regressionMetrics.getMeanAbsoluteError());
        System.out.format(
            "Model mean absolute percentage error: %.2f%%\n",
            regressionMetrics.getMeanAbsolutePercentageError());
        System.out.format("Model rsquared: %.2f%%\n", regressionMetrics.getRSquared());
      }
    }
  }
  // [END automl_tables_display_evaluation]

  // [START automl_tables_deploy_model]
  /**
   * Demonstrates using the AutoML client to deploy model.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   */
  public static void deployModel(String projectId, String computeRegion, String modelId)
      throws IOException, ExecutionException, InterruptedException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

      // Build deploy model request.
      DeployModelRequest deployModelRequest =
          DeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

      // Deploy a model with the deploy model request.
      OperationFuture<Empty, OperationMetadata> future =
          client.deployModelAsync(deployModelRequest);

      // Display the deployment details of model.
      System.out.println("Deployment Details:");
      System.out.format("\tName: %s\n", future.getName());
      System.out.println("\tMetadata:");
      System.out.println(future.getMetadata());
      System.out.format("Model deployment finished. %s\n", future.get());
    }
  }
  // [END automl_tables_deploy_model]

  // [START automl_tables_undeploy_model]
  /**
   * Demonstrates using the AutoML client to undelpoy model.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   */
  public static void undeployModel(String projectId, String computeRegion, String modelId)
      throws IOException, ExecutionException, InterruptedException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

      // Build undeploy model request.
      UndeployModelRequest undeployModelRequest =
          UndeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

      // Undeploy a model with the undeploy model request.
      OperationFuture<Empty, OperationMetadata> future =
          client.undeployModelAsync(undeployModelRequest);

      // Display the undeployment details of model.
      System.out.println("Undeployment Details:");
      System.out.format("\tName: %s\n", future.getName());
      System.out.println("\tMetadata:");
      System.out.println(future.getMetadata());
      System.out.format("Model undeployment finished. %s\n", future.get());
    }
  }
  // [END automl_tables_undeploy_model]

  // [START automl_tables_delete_model]
  /**
   * Demonstrates using the AutoML client to delete a model.
   *
   * @param projectId the Id of the project.
   * @param modelId the Id of the model.
   */
  public static void deleteModel(String projectId, String modelId)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, "us-central1", modelId);

      // Delete a model.
      Empty response = client.deleteModelAsync(modelFullId).get();

      System.out.println("Model deletion started...");
      System.out.format("Model deleted. %s\n", response);
    }
  }
  // [END automl_tables_delete_model]
}
