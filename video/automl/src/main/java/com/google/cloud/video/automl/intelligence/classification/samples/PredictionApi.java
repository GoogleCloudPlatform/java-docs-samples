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

package com.google.cloud.video.automl.intelligence.classification.samples;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

// Imports the Google Cloud client library
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.BatchPredictInputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictOutputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictRequest;
import com.google.cloud.automl.v1beta1.BatchPredictResult;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Google Cloud AutoML Video Intelligence Classification API sample application. Example usage: mvn
 * package exec:java
 * -Dexec.mainClass='com.google.cloud.video.automl.intelligence.classification.samples.PredictionApi'
 * -Dexec.args='predict [modelId] [path-to-input-file] [path-to-output-directory]'
 */
public class PredictionApi {

  // [START automl_video_intelligence_classification_predict]
  /**
   * Demonstrates using the AutoML client to classify the video intelligence
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used for video intelligence classification.
   * @param inputUri the GCS bucket path of csv file which contains path of the video to be
   *     classified.
   * @param outputUriPrefix the output GCS bucket folder path which contains one csv file and json
   *     file for each video classification.
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void predict(
      String projectId,
      String computeRegion,
      String modelId,
      String inputUri,
      String outputUriPrefix)
      throws IOException, InterruptedException, ExecutionException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Set the input URI
    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Get multiple training data files to be imported
    String[] inputUris = inputUri.split(",");
    for (String inputFilePath : inputUris) {
      gcsSource.addInputUris(inputFilePath);
    }

    // Set the Batch Input Configuration
    BatchPredictInputConfig batchInputConfig =
        BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();

    // Set the output URI
    GcsDestination.Builder gcsDestination = GcsDestination.newBuilder();
    gcsDestination.setOutputUriPrefix(outputUriPrefix);

    // Set the Batch Input Configuration
    BatchPredictOutputConfig batchOutputConfig =
        BatchPredictOutputConfig.newBuilder().setGcsDestination(gcsDestination).build();

    // Set the modelName, input and output config in the batch prediction
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
        String.format("Operation Name: %s", operation.getInitialFuture().get().getName()));
  }
  // [END automl_video_intelligence_classification_predict]

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
    predictParser.addArgument("inputUri");
    predictParser.addArgument("outputUriPrefix");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("predict")) {
        predict(
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
