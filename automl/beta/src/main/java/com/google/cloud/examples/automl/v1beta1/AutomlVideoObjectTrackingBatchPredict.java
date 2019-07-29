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
// DO NOT EDIT! This is a generated sample ("LongRunningRequestAsync",  "automl_video_object_tracking_batch_predict")
// sample-metadata:
//   title: AutoML Batch Predict
//   description: AutoML Batch Predict
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingBatchPredict [--args='[--gcs_output_prefix "[gs://your-output-bucket/your-object-id]"] [--model_id "[Model ID]"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.BatchPredictInputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictOutputConfig;
import com.google.cloud.automl.v1beta1.BatchPredictRequest;
import com.google.cloud.automl.v1beta1.BatchPredictResult;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingBatchPredict {
  // [START automl_video_object_tracking_batch_predict]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.BatchPredictInputConfig;
   * import com.google.cloud.automl.v1beta1.BatchPredictOutputConfig;
   * import com.google.cloud.automl.v1beta1.BatchPredictRequest;
   * import com.google.cloud.automl.v1beta1.BatchPredictResult;
   * import com.google.cloud.automl.v1beta1.GcsDestination;
   * import com.google.cloud.automl.v1beta1.GcsSource;
   * import com.google.cloud.automl.v1beta1.ModelName;
   * import com.google.cloud.automl.v1beta1.PredictionServiceClient;
   * import java.util.Arrays;
   * import java.util.HashMap;
   * import java.util.List;
   * import java.util.Map;
   */

  /**
   * AutoML Batch Predict
   *
   * @param gcsOutputPrefix Identifies where to store the output of your prediction request in your
   *     Google Cloud Storage bucket. You must have write permissions to the Google Cloud Storage
   *     bucket.
   * @param modelId Model ID, e.g. VOT1234567890123456789
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleBatchPredict(String gcsOutputPrefix, String modelId, String project) {
    try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create()) {
      // gcsOutputPrefix = "[gs://your-output-bucket/your-object-id]";
      // modelId = "[Model ID]";
      // project = "[Google Cloud Project ID]";
      ModelName name = ModelName.of(project, "us-central1", modelId);
      String inputUrisElement =
          "gs://automl-video-datasets/youtube_8m_videos_animal_batchpredict.csv";
      List<String> inputUris = Arrays.asList(inputUrisElement);
      GcsSource gcsSource = GcsSource.newBuilder().addAllInputUris(inputUris).build();
      BatchPredictInputConfig inputConfig =
          BatchPredictInputConfig.newBuilder().setGcsSource(gcsSource).build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(gcsOutputPrefix).build();
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
      BatchPredictResult response = predictionServiceClient.batchPredictAsync(request).get();
      System.out.println("Batch Prediction results saved to Cloud Storage");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_batch_predict]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("gcs_output_prefix").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("model_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String gcsOutputPrefix =
        cl.getOptionValue("gcs_output_prefix", "[gs://your-output-bucket/your-object-id]");
    String modelId = cl.getOptionValue("model_id", "[Model ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleBatchPredict(gcsOutputPrefix, modelId, project);
  }
}
