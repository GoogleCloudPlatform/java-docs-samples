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
import com.google.cloud.automl.v1beta1.PredictRequest;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.automl.v1beta1.Row;
import com.google.cloud.automl.v1beta1.TablesAnnotation;
import com.google.cloud.automl.v1beta1.TablesModelColumnInfo;
import com.google.protobuf.Value;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PredictionApi {

  // [START automl_tables_predict]
  // Demonstrates using the AutoML client to request prediction from automl tables using csv.
  public static void predict(String projectId, String modelId, List<Value> values)
      throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String modelId = "YOUR_MODEL_ID";
    // values should match the input expected by your model.
    // List<Value> values = new ArrayList<>();
    // values.add(Value.newBuilder().setBoolValue(true).build());
    // values.add(Value.newBuilder().setNumberValue(10).build());
    // values.add(Value.newBuilder().setStringValue("YOUR_STRING").build());

    try (PredictionServiceClient client = PredictionServiceClient.create()) {
      ModelName name = ModelName.of(projectId, "us-central1", modelId);
      Row row = Row.newBuilder().addAllValues(values).build();
      ExamplePayload payload = ExamplePayload.newBuilder().setRow(row).build();
      PredictRequest request =
          PredictRequest.newBuilder()
                  .setName(name.toString())
                  .setPayload(payload)
                  .putParams("feature_importance", "true")
                  .build();

      PredictResponse response = client.predict(request);

      System.out.println("Prediction results:");
      List<AnnotationPayload> annotationPayloadList = response.getPayloadList();
      if (response.getPayloadCount() == 1) {
        TablesAnnotation tablesAnnotation = annotationPayloadList.get(0).getTables();
        System.out.format(
            "\tRegression result: %.3f\n", tablesAnnotation.getValue().getNumberValue());
        // Get features of top importance
        for (TablesModelColumnInfo info : tablesAnnotation.getTablesModelColumnInfoList()) {
          System.out.format(
              "Column: %s - Importance: %.2f\n",
              info.getColumnDisplayName(), info.getFeatureImportance());
        }
      } else {
        for (AnnotationPayload annotationPayload : annotationPayloadList) {
          TablesAnnotation tablesAnnotation = annotationPayload.getTables();
          System.out.format(
              "\tClassification label: %s\t\tClassification score: %.3f\n",
              tablesAnnotation.getValue().getStringValue(), tablesAnnotation.getScore());
          // Get features of top importance
          for (TablesModelColumnInfo info : tablesAnnotation.getTablesModelColumnInfoList()) {
            System.out.format(
                "Column: %s - Importance: %.2f\n",
                info.getColumnDisplayName(), info.getFeatureImportance());
          }
        }
      }
    }
  }
  // [END automl_tables_predict]

  // [START automl_tables_predict_using_gcs_source_and_gcs_dest]
  // Demonstrates using the AutoML client to request prediction from automl tables using GCS.
  public static void batchPredictionUsingGcs(
      String projectId, String modelId, String inputUri, String outputUri)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String modelId = "YOUR_MODEL_ID";
    // String inputUri = "gs://YOUR_BUCKET_ID/path_to_your_input_file.csv";
    // String outputUri = "gs://YOUR_BUCKET_ID/path_to_save_results/";

    // Create client for prediction service.
    try (PredictionServiceClient predictionClient = PredictionServiceClient.create()) {

      // Get full path of model.
      ModelName modelName = ModelName.of(projectId, "us-central1", modelId);

      GcsSource gcsSource = GcsSource.newBuilder().addInputUris(inputUri).build();
      BatchPredictInputConfig batchInputConfig =
          BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(outputUri).build();
      BatchPredictOutputConfig batchOutputConfig =
          BatchPredictOutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
      BatchPredictRequest batchRequest =
          BatchPredictRequest.newBuilder()
              .setName(modelName.toString())
              .setInputConfig(batchInputConfig)
              .setOutputConfig(batchOutputConfig)
              .build();

      OperationFuture<BatchPredictResult, OperationMetadata> future =
          predictionClient.batchPredictAsync(batchRequest);

      System.out.println("Waiting for operation to complete...");
      BatchPredictResult response = future.get();
      System.out.println("Batch Prediction results saved to specified Cloud Storage bucket.");
    }
  }
  // [END automl_tables_predict_using_gcs_source_and_gcs_dest]

  // [START automl_tables_predict_using_bq_source_and_bq_dest]
  // Demonstrates using the AutoML client to request prediction from automl tables using bigQuery.
  public static void batchPredictionUsingBigQuery(
      String projectId, String modelId, String inputUri, String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String modelId = "YOUR_MODEL_ID";
    // String inputUri = "bq://path_to_your_table";
    // String outputUri = "bq://path_to_save_results";

    // Create client for prediction service.
    try (PredictionServiceClient predictionClient = PredictionServiceClient.create()) {
      ModelName modelName = ModelName.of(projectId, "us-central1", modelId);
      BigQuerySource bigQuerySource = BigQuerySource.newBuilder().setInputUri(inputUri).build();
      BatchPredictInputConfig batchInputConfig =
          BatchPredictInputConfig.newBuilder().setBigquerySource(bigQuerySource).build();
      BigQueryDestination bigQueryDestination =
          BigQueryDestination.newBuilder().setOutputUri(outputUriPrefix).build();
      BatchPredictOutputConfig batchOutputConfig =
          BatchPredictOutputConfig.newBuilder().setBigqueryDestination(bigQueryDestination).build();

      BatchPredictRequest batchRequest =
          BatchPredictRequest.newBuilder()
              .setName(modelName.toString())
              .setInputConfig(batchInputConfig)
              .setOutputConfig(batchOutputConfig)
              .build();

      OperationFuture<BatchPredictResult, OperationMetadata> future =
          predictionClient.batchPredictAsync(batchRequest);

      System.out.println("Waiting for operation to complete...");
      BatchPredictResult response = future.get();
      System.out.println("Batch Prediction results saved to specified Cloud Storage bucket.");
    }
  }
  // [END automl_tables_predict_using_bq_source_and_bq_dest]
}
