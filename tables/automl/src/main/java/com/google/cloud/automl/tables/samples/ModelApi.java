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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import com.google.cloud.automl.v1beta1.TablesModelMetadata;
import com.google.cloud.automl.v1beta1.UndeployModelRequest;
import com.google.longrunning.ListOperationsRequest;
import com.google.longrunning.Operation;
import com.google.protobuf.Empty;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Google Cloud AutoML Tables API sample application. Example usage: mvn package exec:java
 * -Dexec.mainClass='com.google.cloud.automl.tables.samples.ModelApi' -Dexec.args='create_model
 * [datasetId] [ModelName]'
 */
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
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
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
    AutoMlClient client = AutoMlClient.create();

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

    System.out.println(
        String.format("Training operation name: %s", response.getInitialFuture().get().getName()));
    System.out.println("Training started...");
  }
  // [END automl_tables_create_model]

  // [START automl_tables_get_operation_status]
  /**
   * Demonstrates using the AutoML client to get operation status.
   *
   * @param operationFullId the complete name of a operation. For example, the name of your
   *     operation is projects/[projectId]/locations/us-central1/operations/[operationId].
   * @throws IOException
   */
  public static void getOperationStatus(String operationFullId) throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the latest state of a long-running operation.
    Operation response = client.getOperationsClient().getOperation(operationFullId);

    // Display operation details.
    System.out.println(String.format("Operation details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(String.format("\t\tType Url: %s", response.getMetadata().getTypeUrl()));
    System.out.println(
        String.format(
            "\t\tValue: %s", response.getMetadata().getValue().toStringUtf8().replace("\n", "")));
    System.out.println(String.format("\tDone: %s", response.getDone()));
    if (response.hasResponse()) {
      System.out.println("\tResponse:");
      System.out.println(String.format("\t\tType Url: %s", response.getResponse().getTypeUrl()));
      System.out.println(
          String.format(
              "\t\tValue: %s", response.getResponse().getValue().toStringUtf8().replace("\n", "")));
    }
    if (response.hasError()) {
      System.out.println("\tResponse:");
      System.out.println(String.format("\t\tError code: %s", response.getError().getCode()));
      System.out.println(String.format("\t\tError message: %s", response.getError().getMessage()));
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
   * @throws IOException
   */
  public static void listOperationsStatus(String projectId, String computeRegion, String filter)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

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
      System.out.println(String.format("Operation details:"));
      System.out.println(String.format("\tName: %s", operation.getName()));
      System.out.println(String.format("\tMetadata:"));
      System.out.println(String.format("\t\tType Url: %s", operation.getMetadata().getTypeUrl()));
      System.out.println(
          String.format(
              "\t\tValue: %s",
              operation.getMetadata().getValue().toStringUtf8().replace("\n", "")));

      System.out.println(String.format("\tDone: %s", operation.getDone()));
      if (operation.hasResponse()) {
        System.out.println("\tResponse:");
        System.out.println(String.format("\t\tType Url: %s", operation.getResponse().getTypeUrl()));
        System.out.println(
            String.format(
                "\t\tValue: %s",
                operation.getResponse().getValue().toStringUtf8().replace("\n", "")));
      }
      if (operation.hasError()) {
        System.out.println("\tResponse:");
        System.out.println(String.format("\t\tError code: %s", operation.getError().getCode()));
        System.out.println(
            String.format("\t\tError message: %s", operation.getError().getMessage()));
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
   * @throws IOException
   */
  public static void listModels(String projectId, String computeRegion, String filter)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

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
      System.out.println("Tables Model Metadata: ");
      System.out.println(
          String.format(
              "\tTraining budget: %s",
              model.getTablesModelMetadata().getTrainBudgetMilliNodeHours()));
      System.out.println(
          String.format(
              "\tTraining cost: %s", model.getTablesModelMetadata().getTrainCostMilliNodeHours()));

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
      System.out.println(String.format("Model create time: %s", createTime));

      System.out.println(String.format("Model deployment state: %s", model.getDeploymentState()));
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
   * @throws IOException
   */
  public static void getModel(String projectId, String computeRegion, String modelId)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Get complete detail of the model.
    Model model = client.getModel(modelFullId);

    // Display the model information.
    System.out.println(String.format("Model name: %s", model.getName()));
    System.out.println(
        String.format(
            "Model Id: %s", model.getName().split("/")[model.getName().split("/").length - 1]));
    System.out.println(String.format("Model display name: %s", model.getDisplayName()));
    System.out.println(String.format("Dataset Id: %s", model.getDatasetId()));
    System.out.println("Tables Model Metadata: ");
    System.out.println(
        String.format(
            "\tTraining budget: %s",
            model.getTablesModelMetadata().getTrainBudgetMilliNodeHours()));
    System.out.println(
        String.format(
            "\tTraining cost: %s", model.getTablesModelMetadata().getTrainBudgetMilliNodeHours()));

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Model create time: %s", createTime));

    System.out.println(String.format("Model deployment state: %s", model.getDeploymentState()));
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
   * @throws IOException
   */
  public static void exportEvaluatedExamples(
      String projectId, String computeRegion, String modelId, String bigQueryUri)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

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

    System.out.println(String.format("Operation name: %s", response));
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
   * @throws IOException
   */
  public static void listModelEvaluations(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // Instantiates a client.
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

      int regressionLength = element.getRegressionEvaluationMetrics().toString().length();
      int classificationLength = element.getClassificationEvaluationMetrics().toString().length();

      if (classificationLength > 0) {
        ClassificationEvaluationMetrics classificationMetrics =
            element.getClassificationEvaluationMetrics();
        List<ConfidenceMetricsEntry> confidenceMetricsEntries =
            classificationMetrics.getConfidenceMetricsEntryList();

        // Display tables classification model information.
        System.out.println("Table classification evaluation metrics:");
        System.out.println(String.format("\tModel au_prc: %f ", classificationMetrics.getAuPrc()));
        System.out.println(String.format("\tModel au_roc: %f ", classificationMetrics.getAuRoc()));
        System.out.println(
            String.format("\tModel log loss: %f ", classificationMetrics.getLogLoss()));

        if (!confidenceMetricsEntries.isEmpty()) {
          System.out.println("\tConfidence metrics entries:");
          // Showing table classification evaluation metrics.
          for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
            System.out.println(
                String.format(
                    "\t\tModel confidence threshold: %.2f ",
                    confidenceMetricsEntry.getConfidenceThreshold()));
            System.out.println(
                String.format(
                        "\t\tModel precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
                    + '%');
            System.out.println(
                String.format("\t\tModel recall: %.2f ", confidenceMetricsEntry.getRecall() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "\t\tModel f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "\t\tModel precision@1: %.2f ",
                        confidenceMetricsEntry.getPrecisionAt1() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "\t\tModel recall@1: %.2f ", confidenceMetricsEntry.getRecallAt1() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "\t\tModel f1 score@1: %.2f ", confidenceMetricsEntry.getF1ScoreAt1() * 100)
                    + '%');
            System.out.println(
                String.format(
                    "\t\tModel false positive rate: %.2f ",
                    confidenceMetricsEntry.getFalsePositiveRate()));
            System.out.println(
                String.format(
                    "\t\tModel true positive count: %s ",
                    confidenceMetricsEntry.getTruePositiveCount()));
            System.out.println(
                String.format(
                    "\t\tModel false positive count: %s ",
                    confidenceMetricsEntry.getFalsePositiveCount()));
            System.out.println(
                String.format(
                    "\t\tModel true negative count: %s ",
                    confidenceMetricsEntry.getTrueNegativeCount()));
            System.out.println(
                String.format(
                    "\t\tModel false negative count: %s ",
                    confidenceMetricsEntry.getFalseNegativeCount()));
            System.out.println(
                String.format(
                        "\t\tModel position threshold: %s ",
                        confidenceMetricsEntry.getPositionThreshold())
                    + '\n');
          }
        }
      } else if (regressionLength > 0) {
        RegressionEvaluationMetrics regressionMetrics = element.getRegressionEvaluationMetrics();
        System.out.println("Table regression evaluation metrics:");
        // Showing tables regression evaluation metrics
        System.out.println(
            String.format(
                    "\tModel root mean squared error: ",
                    regressionMetrics.getRootMeanSquaredError())
                + '%');
        System.out.println(
            String.format("\tModel mean absolute error: ", regressionMetrics.getMeanAbsoluteError())
                + '%');
        System.out.println(
            String.format(
                    "\tModel mean absolute percentage error: ",
                    regressionMetrics.getMeanAbsolutePercentageError())
                + '%');
        System.out.println(
            String.format("\tModel rsquared: ", regressionMetrics.getRSquared()) + '%' + '\n');
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
   * @throws IOException
   */
  public static void getModelEvaluation(
      String projectId, String computeRegion, String modelId, String modelEvaluationId)
      throws IOException {
    // Instantiates a client.
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

    int regressionLength = response.getRegressionEvaluationMetrics().toString().length();
    int classificationLength = response.getClassificationEvaluationMetrics().toString().length();

    if (classificationLength > 0) {
      ClassificationEvaluationMetrics classificationMetrics =
          response.getClassificationEvaluationMetrics();
      List<ConfidenceMetricsEntry> confidenceMetricsEntries =
          classificationMetrics.getConfidenceMetricsEntryList();
      if (!confidenceMetricsEntries.isEmpty()) {
        System.out.println("Table classification evaluation metrics:");
        System.out.println(String.format("\tModel au_prc: %f ", classificationMetrics.getAuPrc()));
        System.out.println(String.format("\tModel au_roc: %f ", classificationMetrics.getAuRoc()));
        System.out.println(
            String.format("\tModel log loss: %f ", classificationMetrics.getLogLoss()));
        System.out.println("\tConfidence metrics entries:");
        // Showing tables classification evaluation metrics.
        for (ConfidenceMetricsEntry confidenceMetricsEntry : confidenceMetricsEntries) {
          System.out.println(
              String.format(
                  "\t\tModel confidence threshold: %.2f ",
                  confidenceMetricsEntry.getConfidenceThreshold()));
          System.out.println(
              String.format(
                      "\t\tModel precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
                  + '%');
          System.out.println(
              String.format("\t\tModel recall: %.2f ", confidenceMetricsEntry.getRecall() * 100)
                  + '%');
          System.out.println(
              String.format("\t\tModel f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
                  + '%');
          System.out.println(
              String.format(
                      "\t\tModel precision@1: %.2f ",
                      confidenceMetricsEntry.getPrecisionAt1() * 100)
                  + '%');
          System.out.println(
              String.format(
                      "\t\tModel recall@1: %.2f ", confidenceMetricsEntry.getRecallAt1() * 100)
                  + '%');
          System.out.println(
              String.format(
                      "\t\tModel f1 score@1: %.2f ", confidenceMetricsEntry.getF1ScoreAt1() * 100)
                  + '%');
          System.out.println(
              String.format(
                  "\t\tModel false positive rate: %.2f ",
                  confidenceMetricsEntry.getFalsePositiveRate()));
          System.out.println(
              String.format(
                  "\t\tModel true positive count: %s ",
                  confidenceMetricsEntry.getTruePositiveCount()));
          System.out.println(
              String.format(
                  "\t\tModel false positive count: %s ",
                  confidenceMetricsEntry.getFalsePositiveCount()));
          System.out.println(
              String.format(
                  "\t\tModel true negative count: %s ",
                  confidenceMetricsEntry.getTrueNegativeCount()));
          System.out.println(
              String.format(
                  "\t\tModel false negative count: %s ",
                  confidenceMetricsEntry.getFalseNegativeCount()));
          System.out.println(
              String.format(
                      "\t\tModel position threshold: %s ",
                      confidenceMetricsEntry.getPositionThreshold())
                  + '\n');
        }
      }
    } else if (regressionLength > 0) {
      RegressionEvaluationMetrics regressionMetrics = response.getRegressionEvaluationMetrics();
      System.out.println("Table regression evaluation metrics:");
      // Showing tables regression evaluation metrics.
      System.out.println(
          String.format(
                  "\tModel root mean squared error: ", regressionMetrics.getRootMeanSquaredError())
              + '%');
      System.out.println(
          String.format("\tModel mean absolute error: ", regressionMetrics.getMeanAbsoluteError())
              + '%');
      System.out.println(
          String.format(
                  "\tModel mean absolute percentage error: ",
                  regressionMetrics.getMeanAbsolutePercentageError())
              + '%');
      System.out.println(
          String.format("\tModel rsquared: ", regressionMetrics.getRSquared()) + '%');
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
   * @throws IOException
   */
  public static void displayEvaluation(
      String projectId, String computeRegion, String modelId, String filter) throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

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
            System.out.println(
                String.format("Model precision: %.2f ", confidenceMetricsEntry.getPrecision() * 100)
                    + '%');
            System.out.println(
                String.format("Model recall: %.2f ", confidenceMetricsEntry.getRecall() * 100)
                    + '%');
            System.out.println(
                String.format("Model f1 score: %.2f ", confidenceMetricsEntry.getF1Score() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "Model precision@1: %.2f ", confidenceMetricsEntry.getPrecisionAt1() * 100)
                    + '%');
            System.out.println(
                String.format("Model recall@1: %.2f ", confidenceMetricsEntry.getRecallAt1() * 100)
                    + '%');
            System.out.println(
                String.format(
                        "Model f1 score@1: %.2f ", confidenceMetricsEntry.getF1ScoreAt1() * 100)
                    + '%');
          }
        }
      }
    } else if (regressionLength > 0) {
      RegressionEvaluationMetrics regressionMetrics =
          modelEvaluation.getRegressionEvaluationMetrics();
      System.out.println("Table regression evaluation metrics:");
      // Showing tables regression evaluation metrics.
      System.out.println(
          String.format(
                  "Model root mean squared error: ", regressionMetrics.getRootMeanSquaredError())
              + '%');
      System.out.println(
          String.format("Model mean absolute error: ", regressionMetrics.getMeanAbsoluteError())
              + '%');
      System.out.println(
          String.format(
                  "Model mean absolute percentage error: ",
                  regressionMetrics.getMeanAbsolutePercentageError())
              + '%');
      System.out.println(String.format("Model rsquared: ", regressionMetrics.getRSquared()) + '%');
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
   * @throws IOException
   */
  public static void deployModel(String projectId, String computeRegion, String modelId)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Build deploy model request.
    DeployModelRequest deployModelRequest =
        DeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

    // Deploy a model with the deploy model request.
    Operation response = client.deployModel(deployModelRequest);

    // Display the deployment details of model.
    System.out.println(String.format("Deployment Details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(String.format("\t\tType Url: %s", response.getMetadata().getTypeUrl()));
    System.out.println(
        String.format(
            "\t\tValue: %s", response.getMetadata().getValue().toStringUtf8().replace("\n", "")));
  }
  // [END automl_tables_deploy_model]

  // [START automl_tables_deploy_model]
  /**
   * Demonstrates using the AutoML client to undelpoy model.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @throws IOException
   */
  public static void undeployModel(String projectId, String computeRegion, String modelId)
      throws IOException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Build undeploy model request.
    UndeployModelRequest undeployModelRequest =
        UndeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

    // Undeploy a model with the undeploy model request.
    Operation response = client.undeployModel(undeployModelRequest);

    // Display the undeployment details of model.
    System.out.println(String.format("Undeployment Details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(String.format("\t\tType Url: %s", response.getMetadata().getTypeUrl()));
    System.out.println(
        String.format(
            "\t\tValue: %s", response.getMetadata().getValue().toStringUtf8().replace("\n", "")));
  }
  // [END automl_tables_deploy_model]

  // [START automl_tables_delete_model]
  /**
   * Demonstrates using the AutoML client to delete a model.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model.
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void deleteModel(String projectId, String computeRegion, String modelId)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Delete a model.
    Empty response = client.deleteModelAsync(modelFullId).get();

    System.out.println("Model deletion started...");
    System.out.println(String.format("Model deleted. %s", response));
  }
  // [END automl_tables_delete_model]

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    ModelApi modelApi = new ModelApi();
    modelApi.argsHelper(args);
  }

  public void argsHelper(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    ArgumentParser parser =
        ArgumentParsers.newFor("ModelApi")
            .build()
            .defaultHelp(true)
            .description("Model API operations.");
    Subparsers subparsers = parser.addSubparsers().dest("command");

    Subparser createModelParser = subparsers.addParser("create_model");
    createModelParser.addArgument("datasetId");
    createModelParser.addArgument("tableId");
    createModelParser.addArgument("columnId");
    createModelParser.addArgument("modelName");
    createModelParser.addArgument("trainBudget").nargs("?").setDefault("0");

    Subparser getOperationStatusParser = subparsers.addParser("get_operation_status");
    getOperationStatusParser.addArgument("operationFullId");

    Subparser listOperationsStatusParser = subparsers.addParser("list_operations_status");
    listOperationsStatusParser.addArgument("filter").nargs("?").setDefault("");

    Subparser listModelsParser = subparsers.addParser("list_models");
    listModelsParser.addArgument("filter").nargs("?").setDefault("tablesModelMetadata:*");

    Subparser getModelParser = subparsers.addParser("get_model");
    getModelParser.addArgument("modelId");

    Subparser exportEvaluatedExamplesParser = subparsers.addParser("export_evaluated_examples");
    exportEvaluatedExamplesParser.addArgument("modelId");
    exportEvaluatedExamplesParser.addArgument("outputUri");

    Subparser listModelEvaluationsParser = subparsers.addParser("list_model_evaluations");
    listModelEvaluationsParser.addArgument("modelId");
    listModelEvaluationsParser.addArgument("filter").nargs("?").setDefault("");

    Subparser getModelEvaluationParser = subparsers.addParser("get_model_evaluation");
    getModelEvaluationParser.addArgument("modelId");
    getModelEvaluationParser.addArgument("modelEvaluationId");

    Subparser displayEvaluationParser = subparsers.addParser("display_evaluation");
    displayEvaluationParser.addArgument("modelId");
    displayEvaluationParser.addArgument("filter").nargs("?").setDefault("");

    Subparser deployModelParser = subparsers.addParser("deploy_model");
    deployModelParser.addArgument("modelId");

    Subparser undeployModelParser = subparsers.addParser("undeploy_model");
    undeployModelParser.addArgument("modelId");

    Subparser deleteModelParser = subparsers.addParser("delete_model");
    deleteModelParser.addArgument("modelId");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("create_model")) {
        createModel(
            projectId,
            computeRegion,
            ns.getString("datasetId"),
            ns.getString("tableId"),
            ns.getString("columnId"),
            ns.getString("modelName"),
            ns.getString("trainBudget"));
      }
      if (ns.get("command").equals("get_operation_status")) {
        getOperationStatus(ns.getString("operationFullId"));
      }
      if (ns.get("command").equals("list_operations_status")) {
        listOperationsStatus(projectId, computeRegion, ns.getString("filter"));
      }
      if (ns.get("command").equals("list_models")) {
        listModels(projectId, computeRegion, ns.getString("filter"));
      }
      if (ns.get("command").equals("get_model")) {
        getModel(projectId, computeRegion, ns.getString("modelId"));
      }
      if (ns.get("command").equals("export_evaluated_examples")) {
        exportEvaluatedExamples(
            projectId, computeRegion, ns.getString("modelId"), ns.getString("outputUri"));
      }
      if (ns.get("command").equals("list_model_evaluations")) {
        listModelEvaluations(
            projectId, computeRegion, ns.getString("modelId"), ns.getString("filter"));
      }
      if (ns.get("command").equals("get_model_evaluation")) {
        getModelEvaluation(
            projectId, computeRegion, ns.getString("modelId"), ns.getString("modelEvaluationId"));
      }
      if (ns.get("command").equals("display_evaluation")) {
        displayEvaluation(
            projectId, computeRegion, ns.getString("modelId"), ns.getString("filter"));
      }
      if (ns.get("command").equals("deploy_model")) {
        deployModel(projectId, computeRegion, ns.getString("modelId"));
      }
      if (ns.get("command").equals("undeploy_model")) {
        undeployModel(projectId, computeRegion, ns.getString("modelId"));
      }
      if (ns.get("command").equals("delete_model")) {
        deleteModel(projectId, computeRegion, ns.getString("modelId"));
      }
      System.exit(1);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}
