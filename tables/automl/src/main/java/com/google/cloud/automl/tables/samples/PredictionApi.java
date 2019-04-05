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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

// Imports the Google Cloud client library
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.BatchPredictInputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictOutputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictRequest;
import com.google.cloud.automl.v1beta1.BatchPredictResult;
import com.google.cloud.automl.v1beta1.BigQueryDestination;
import com.google.cloud.automl.v1beta1.BigQuerySource;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.automl.v1beta1.Row;
import com.google.protobuf.Value;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Google Cloud AutoML Tables API sample application. Example usage: mvn package exec:java
 * -Dexec.mainClass='com.google.cloud.automl.tables.samples.PredictionApi' -Dexec.args='predict
 * [modelId] [path-to-input-file]'
 */
public class PredictionApi {

  // [START automl_tables_predict]
  /**
   * Demonstrates using the AutoML client to request prediction from automl tables using csv.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used for the prediction.
   * @param filePath the Local text file path of the content.
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void predict(
      String projectId, String computeRegion, String modelId, String filePath)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Read the csv file content for prediction.
    Reader in = new FileReader(filePath);
    Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
    for (CSVRecord record : records) {
      int n = record.size();

      // Add all the values.
      List<Value> values = new ArrayList<Value>();
      for (int i = 0; i < n; i++) {
        Value value = Value.newBuilder().setStringValue(record.get(i)).build();
        values.add(value);
      }
      Iterable<Value> allValues = values;

      // Build the row.
      Row row = Row.newBuilder().addAllValues(allValues).build();

      // Build the payload data.
      ExamplePayload examplePayload = ExamplePayload.newBuilder().setRow(row).build();

      // params is additional domain-specific parameters.
      // currently there is no additional parameters supported.
      Map<String, String> params = new HashMap<String, String>();

      PredictResponse response = predictionClient.predict(modelName, examplePayload, params);
      System.out.println("Prediction results:");
      List<AnnotationPayload> annotationPayloadList = response.getPayloadList();
      if (response.getPayloadCount() == 1) {
        System.out.println(
            String.format(
                "\tRegression result: %0.3f",
                annotationPayloadList.get(0).getTables().getValue().getNumberValue()));
      } else {
        for (AnnotationPayload annotationPayload : annotationPayloadList) {
          System.out.println(
              String.format(
                  "\tClassification label: %s\t\tClassification score: %.3f",
                  annotationPayload.getTables().getValue().getStringValue(),
                  annotationPayload.getTables().getScore()));
        }
      }
    }
  }
  // [END automl_tables_predict]

  // [START automl_tables_predict_using_gcs_source_and_gcs_dest]
  /**
   * Demonstrates using the AutoML client to request prediction from automl tables using GCS.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used prediction.
   * @param inputUri Google Cloud Storage URIs.
   * @param outputUriPrefix gcsUri the Destination URI (Google Cloud Storage).
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void batchPredictionUsingGcsSourceAndGcsDest(
      String projectId,
      String computeRegion,
      String modelId,
      String inputUri,
      String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Set the Input URI.
    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Add multiple csv files.
    String[] inputUris = inputUri.split(",");
    for (String addInputUri : inputUris) {
      gcsSource.addInputUris(addInputUri);
    }

    // Set the Batch Input Configuration.
    BatchPredictInputConfig batchInputConfig =
        BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();

    // Set the Output URI.
    GcsDestination.Builder gcsDestination = GcsDestination.newBuilder();
    gcsDestination.setOutputUriPrefix(outputUriPrefix);

    // Set the Batch Output Configuration.
    BatchPredictOutputConfig batchOutputConfig =
        BatchPredictOutputConfig.newBuilder().setGcsDestination(gcsDestination).build();

    // Set the modelName, input and output config in the batch prediction.
    BatchPredictRequest batchRequest =
        BatchPredictRequest.newBuilder()
            .setInputConfig(batchInputConfig)
            .setOutputConfig(batchOutputConfig)
            .setName(modelName.toString())
            .build();

    // Get the latest state of a long-running operation.
    OperationFuture<BatchPredictResult, OperationMetadata> operation =
        predictionClient.batchPredictAsync(batchRequest);

    System.out.println(
        String.format("Operation name: %s", operation.getInitialFuture().get().getName()));
  }
  // [END automl_tables_predict_using_gcs_source_and_gcs_dest]

  // [START automl_tables_predict_using_bq_source_and_gcs_dest]
  /**
   * Demonstrates using the AutoML client to request prediction from automl tables using bigQuery.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used prediction.
   * @param inputUri BigQuery URIs.
   * @param outputUriPrefix gcsUri the Destination URI (Google Cloud Storage).
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void batchPredictionUsingBqSourceAndGcsDest(
      String projectId,
      String computeRegion,
      String modelId,
      String inputUri,
      String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Set the Input URI.
    BigQuerySource.Builder bigQuerySource = BigQuerySource.newBuilder();
    bigQuerySource.setInputUri(inputUri);

    // Set the Batch Input Configuration.
    BatchPredictInputConfig batchInputConfig =
        BatchPredictInputConfig.newBuilder().setBigquerySource(bigQuerySource).build();

    // Set the Output URI.
    GcsDestination.Builder gcsDestination = GcsDestination.newBuilder();
    gcsDestination.setOutputUriPrefix(outputUriPrefix);

    // Set the Batch Output Configuration.
    BatchPredictOutputConfig batchOutputConfig =
        BatchPredictOutputConfig.newBuilder().setGcsDestination(gcsDestination).build();

    // Set the modelName, input and output config in the batch prediction.
    BatchPredictRequest batchRequest =
        BatchPredictRequest.newBuilder()
            .setInputConfig(batchInputConfig)
            .setOutputConfig(batchOutputConfig)
            .setName(modelName.toString())
            .build();

    // Get the latest state of a long-running operation.
    OperationFuture<BatchPredictResult, OperationMetadata> operation =
        predictionClient.batchPredictAsync(batchRequest);

    System.out.println(
        String.format("Operation name: %s", operation.getInitialFuture().get().getName()));
  }
  // [END automl_tables_predict_using_bq_source_and_gcs_dest]

  // [START automl_tables_predict_using_gcs_source_and_bq_dest]
  /**
   * Demonstrates using the AutoML client to request prediction from automl tables using GCS.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used prediction.
   * @param inputUri Google Cloud Storage URIs.
   * @param outputUriPrefix BigQuery URIs.
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void batchPredictionUsingGcsSourceAndBqDest(
      String projectId,
      String computeRegion,
      String modelId,
      String inputUri,
      String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Set the Input URI.
    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Add multiple csv files.
    String[] inputUris = inputUri.split(",");
    for (String addInputUri : inputUris) {
      gcsSource.addInputUris(addInputUri);
    }

    // Set the Batch Input Configuration.
    BatchPredictInputConfig batchInputConfig =
        BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();

    // Set the Output URI.
    BigQueryDestination.Builder bigQueryDestination = BigQueryDestination.newBuilder();
    bigQueryDestination.setOutputUri(outputUriPrefix);

    // Set the Batch Output Configuration.
    BatchPredictOutputConfig batchOutputConfig =
        BatchPredictOutputConfig.newBuilder().setBigqueryDestination(bigQueryDestination).build();

    // Set the modelName, input and output config in the batch prediction.
    BatchPredictRequest batchRequest =
        BatchPredictRequest.newBuilder()
            .setInputConfig(batchInputConfig)
            .setOutputConfig(batchOutputConfig)
            .setName(modelName.toString())
            .build();

    // Get the latest state of a long-running operation.
    OperationFuture<BatchPredictResult, OperationMetadata> operation =
        predictionClient.batchPredictAsync(batchRequest);

    System.out.println(
        String.format("Operation name: %s", operation.getInitialFuture().get().getName()));
  }
  // [END automl_tables_predict_using_gcs_source_and_bq_dest]

  // [START automl_tables_predict_using_bq_source_and_bq_dest]
  /**
   * Demonstrates using the AutoML client to request prediction from automl tables using bigQuery.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used prediction.
   * @param inputUri BigQuery URIs.
   * @param outputUriPrefix BigQuery URIs.
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void batchPredictionUsingBqSourceAndBqDest(
      String projectId,
      String computeRegion,
      String modelId,
      String inputUri,
      String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Set the Input URI.
    BigQuerySource.Builder bigQuerySource = BigQuerySource.newBuilder();
    bigQuerySource.setInputUri(inputUri);

    // Set the Batch Input Configuration.
    BatchPredictInputConfig batchInputConfig =
        BatchPredictInputConfig.newBuilder().setBigquerySource(bigQuerySource).build();

    // Set the Output URI.
    BigQueryDestination.Builder bigQueryDestination = BigQueryDestination.newBuilder();
    bigQueryDestination.setOutputUri(outputUriPrefix);

    // Set the Batch Output Configuration.
    BatchPredictOutputConfig batchOutputConfig =
        BatchPredictOutputConfig.newBuilder().setBigqueryDestination(bigQueryDestination).build();

    // Set the modelName, input and output config in the batch prediction.
    BatchPredictRequest batchRequest =
        BatchPredictRequest.newBuilder()
            .setInputConfig(batchInputConfig)
            .setOutputConfig(batchOutputConfig)
            .setName(modelName.toString())
            .build();

    // Get the latest state of a long-running operation.
    OperationFuture<BatchPredictResult, OperationMetadata> operation =
        predictionClient.batchPredictAsync(batchRequest);

    System.out.println(
        String.format("Operation name: %s", operation.getInitialFuture().get().getName()));
  }
  // [END automl_tables_predict_using_bq_source_and_bq_dest]

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    PredictionApi predictionApi = new PredictionApi();
    predictionApi.argsHelper(args);
  }

  public void argsHelper(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    ArgumentParser parser =
        ArgumentParsers.newFor("PredictionApi")
            .build()
            .defaultHelp(true)
            .description("Prediction API operations.");
    Subparsers subparsers = parser.addSubparsers().dest("command");

    Subparser predictParser = subparsers.addParser("predict");
    predictParser.addArgument("modelId");
    predictParser.addArgument("filePath");

    Subparser batchPredictUsingGcsSourceAndGcsDestParser =
        subparsers.addParser("predict_using_gcs_source_and_gcs_dest");
    batchPredictUsingGcsSourceAndGcsDestParser.addArgument("modelId");
    batchPredictUsingGcsSourceAndGcsDestParser.addArgument("inputUri");
    batchPredictUsingGcsSourceAndGcsDestParser.addArgument("outputUriPrefix");

    Subparser batchPredictUsingBqSourceAndGcsDestParser =
        subparsers.addParser("predict_using_bq_source_and_gcs_dest");
    batchPredictUsingBqSourceAndGcsDestParser.addArgument("modelId");
    batchPredictUsingBqSourceAndGcsDestParser.addArgument("inputUri");
    batchPredictUsingBqSourceAndGcsDestParser.addArgument("outputUriPrefix");

    Subparser batchPredictUsingGcsSourceAndBqDestParser =
        subparsers.addParser("predict_using_gcs_source_and_bq_dest");
    batchPredictUsingGcsSourceAndBqDestParser.addArgument("modelId");
    batchPredictUsingGcsSourceAndBqDestParser.addArgument("inputUri");
    batchPredictUsingGcsSourceAndBqDestParser.addArgument("outputUriPrefix");

    Subparser batchPredictUsingBqSourceAndBqDestParser =
        subparsers.addParser("predict_using_bq_source_and_bq_dest");
    batchPredictUsingBqSourceAndBqDestParser.addArgument("modelId");
    batchPredictUsingBqSourceAndBqDestParser.addArgument("inputUri");
    batchPredictUsingBqSourceAndBqDestParser.addArgument("outputUriPrefix");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("predict")) {
        predict(projectId, computeRegion, ns.getString("modelId"), ns.getString("filePath"));
      }
      if (ns.get("command").equals("predict_using_gcs_source_and_gcs_dest")) {
        batchPredictionUsingGcsSourceAndGcsDest(
            projectId,
            computeRegion,
            ns.getString("modelId"),
            ns.getString("inputUri"),
            ns.getString("outputUriPrefix"));
      }
      if (ns.get("command").equals("predict_using_bq_source_and_gcs_dest")) {
        batchPredictionUsingBqSourceAndGcsDest(
            projectId,
            computeRegion,
            ns.getString("modelId"),
            ns.getString("inputUri"),
            ns.getString("outputUriPrefix"));
      }
      if (ns.get("command").equals("predict_using_gcs_source_and_bq_dest")) {
        batchPredictionUsingGcsSourceAndBqDest(
            projectId,
            computeRegion,
            ns.getString("modelId"),
            ns.getString("inputUri"),
            ns.getString("outputUriPrefix"));
      }
      if (ns.get("command").equals("predict_using_bq_source_and_bq_dest")) {
        batchPredictionUsingBqSourceAndBqDest(
            projectId,
            computeRegion,
            ns.getString("modelId"),
            ns.getString("inputUri"),
            ns.getString("outputUriPrefix"));
      }
      System.exit(1);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}
