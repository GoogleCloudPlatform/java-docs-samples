/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// DO NOT EDIT! This is a generated sample ("LongRunningRequestAsync",  "automl_vision_batch_predict")
// sample-metadata:
//   title: AutoML Batch Predict (AutoML Vision)
//   description: AutoML Batch Predict using AutoML Vision
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVisionBatchPredict [--args='[--input_uri "gs://[BUCKET-NAME]/path/to/file-with-image-urls.csv"] [--output_uri "gs://[BUCKET-NAME]/directory-for-output-files/"] [--project "[Google Cloud Project ID]"] [--model_id "[Model ID]"]']

package com.google.cloud.examples.automl.v1beta1;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVisionBatchPredict {
  // [START automl_vision_batch_predict]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.api.gax.longrunning.OperationFuture;
   * import com.google.cloud.automl.v1beta1.BatchPredictInputConfig;
   * import com.google.cloud.automl.v1beta1.BatchPredictOutputConfig;
   * import com.google.cloud.automl.v1beta1.BatchPredictRequest;
   * import com.google.cloud.automl.v1beta1.BatchPredictResult;
   * import com.google.cloud.automl.v1beta1.GcsDestination;
   * import com.google.cloud.automl.v1beta1.GcsSource;
   * import com.google.cloud.automl.v1beta1.ModelName;
   * import com.google.cloud.automl.v1beta1.OperationMetadata;
   * import com.google.cloud.automl.v1beta1.PredictionServiceClient;
   * import java.util.Arrays;
   * import java.util.HashMap;
   * import java.util.List;
   * import java.util.Map;
   */

  /**
   * AutoML Batch Predict using AutoML Vision
   *
   * @param inputUri Google Cloud Storage URI to CSV file in your bucket that contains the paths to
   *     the images to annotate, e.g. gs://[BUCKET-NAME]/path/to/images.csv Each line specifies a
   *     separate path to an image in Google Cloud Storage.
   * @param outputUri Identifies where to store the output of your prediction request in your Google
   *     Cloud Storage bucket. You must have write permissions to the Google Cloud Storage bucket.
   * @param project Required. Your Google Cloud Project ID.
   * @param modelId Model ID, e.g. VOT1234567890123456789
   */
  public static void sampleBatchPredict(
      String inputUri, String outputUri, String project, String modelId) {
    try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create()) {
      // inputUri = "gs://[BUCKET-NAME]/path/to/file-with-image-urls.csv";
      // outputUri = "gs://[BUCKET-NAME]/directory-for-output-files/";
      // project = "[Google Cloud Project ID]";
      // modelId = "[Model ID]";
      ModelName name = ModelName.of(project, "us-central1", modelId);
      List<String> inputUris = Arrays.asList(inputUri);
      GcsSource gcsSource = GcsSource.newBuilder().addAllInputUris(inputUris).build();
      BatchPredictInputConfig inputConfig =
          BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(outputUri).build();
      BatchPredictOutputConfig outputConfig =
          BatchPredictOutputConfig.newBuilder().setGcsDestination(gcsDestination).build();

      // A value from 0.0 to 1.0. When the model detects objects on video frames,
      // it will only produce bounding boxes that have at least this confidence score.
      // The default is 0.5.
      String paramsItem = "0.0";
      Map<String, String> params = new HashMap<>();
      params.put("score_threshold", paramsItem);
      BatchPredictRequest request =
          BatchPredictRequest.newBuilder()
              .setName(name.toString())
              .setInputConfig(inputConfig)
              .setOutputConfig(outputConfig)
              .putAllParams(params)
              .build();
      OperationFuture<BatchPredictResult, OperationMetadata> future =
          predictionServiceClient.batchPredictAsync(request);

      System.out.println("Waiting for operation to complete...");
      BatchPredictResult response = future.get();
      System.out.println("Batch Prediction results saved to specified Cloud Storage bucket.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_vision_batch_predict]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("input_uri").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("output_uri").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("model_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String inputUri =
        cl.getOptionValue("input_uri", "gs://[BUCKET-NAME]/path/to/file-with-image-urls.csv");
    String outputUri =
        cl.getOptionValue("output_uri", "gs://[BUCKET-NAME]/directory-for-output-files/");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");
    String modelId = cl.getOptionValue("model_id", "[Model ID]");

    sampleBatchPredict(inputUri, outputUri, project, modelId);
  }
}
