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
// DO NOT EDIT! This is a generated sample ("RequestPaged",  "automl_video_object_tracking_list_model_evaluations")
// sample-metadata:
//   title: List Model Evaluations
//   description: List Model Evaluations
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingListModelEvaluations [--args='[--project "[Google Cloud Project ID]"] [--model_id "[Model ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.BoundingBoxMetricsEntry;
import com.google.cloud.automl.v1beta1.BoundingBoxMetricsEntry.ConfidenceMetricsEntry;
import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
import com.google.cloud.automl.v1beta1.ModelEvaluation;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.VideoObjectTrackingEvaluationMetrics;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingListModelEvaluations {
  // [START automl_video_object_tracking_list_model_evaluations]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.BoundingBoxMetricsEntry;
   * import com.google.cloud.automl.v1beta1.BoundingBoxMetricsEntry.ConfidenceMetricsEntry;
   * import com.google.cloud.automl.v1beta1.ListModelEvaluationsRequest;
   * import com.google.cloud.automl.v1beta1.ModelEvaluation;
   * import com.google.cloud.automl.v1beta1.ModelName;
   * import com.google.cloud.automl.v1beta1.VideoObjectTrackingEvaluationMetrics;
   */

  /**
   * List Model Evaluations
   *
   * @param modelId Model ID, e.g. VOT1234567890123456789
   */
  public static void sampleListModelEvaluations(String project, String modelId) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // project = "[Google Cloud Project ID]";
      // modelId = "[Model ID]";
      ModelName parent = ModelName.of(project, "us-central1", modelId);
      ListModelEvaluationsRequest request =
          ListModelEvaluationsRequest.newBuilder().setParent(parent.toString()).build();
      for (ModelEvaluation responseItem : autoMlClient.listModelEvaluations(request).iterateAll()) {
        ModelEvaluation evaluation = responseItem;
        System.out.printf("Model evaluation: %s\n", evaluation.getName());
        System.out.printf("Display name: %s\n", evaluation.getDisplayName());
        System.out.printf("Evaluated example count: %s\n", evaluation.getEvaluatedExampleCount());
        VideoObjectTrackingEvaluationMetrics videoMetrics =
            evaluation.getVideoObjectTrackingEvaluationMetrics();
        // The number of video frames used to create this evaluation.
        System.out.printf("Evaluated Frame Count: %s\n", videoMetrics.getEvaluatedFrameCount());
        // The total number of bounding boxes (i.e. summed over all frames)
        // the ground truth used to create this evaluation had.
        //
        System.out.printf(
            "Evaluated Bounding Box Count: %s\n", videoMetrics.getEvaluatedBoundingBoxCount());
        // The single metric for bounding boxes evaluation: the mean_average_precision
        // averaged over all bounding_box_metrics_entries.
        //
        System.out.printf(
            "Bounding Box Mean Average Precision: %s\n",
            videoMetrics.getBoundingBoxMeanAveragePrecision());
        // The bounding boxes match metrics for each Intersection-over-union threshold
        // 0.05,0.10,...,0.95,0.96,0.97,0.98,0.99 and each label confidence threshold
        // 0.05,0.10,...,0.95,0.96,0.97,0.98,0.99 pair.
        //
        for (BoundingBoxMetricsEntry boundingBoxMetricsEntry :
            videoMetrics.getBoundingBoxMetricsEntriesList()) {
          // The intersection-over-union threshold value used to compute this metrics entry.
          //
          System.out.printf("IoU Threshold: %s\n", boundingBoxMetricsEntry.getIouThreshold());
          // The mean average precision, most often close to au_prc.
          //
          System.out.printf(
              "Mean Average Precision: %s\n", boundingBoxMetricsEntry.getMeanAveragePrecision());
          // Metrics for each label-match confidence_threshold from
          // 0.05,0.10,...,0.95,0.96,0.97,0.98,0.99. =
          // Precision-recall curve is derived from them.
          //
          for (BoundingBoxMetricsEntry.ConfidenceMetricsEntry confidenceMetricsEntry :
              boundingBoxMetricsEntry.getConfidenceMetricsEntriesList()) {
            // The confidence threshold value used to compute the metrics.
            System.out.printf(
                "Confidence Threshold: %s\n", confidenceMetricsEntry.getConfidenceThreshold());
            // Recall under the given confidence threshold.
            System.out.printf("Recall %s\n", confidenceMetricsEntry.getRecall());
            // Precision under the given confidence threshold.
            System.out.printf("Precision: %s\n", confidenceMetricsEntry.getPrecision());
            // The harmonic mean of recall and precision.
            System.out.printf("F1 Score: %s\n", confidenceMetricsEntry.getF1Score());
          }
        }
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_list_model_evaluations]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("model_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");
    String modelId = cl.getOptionValue("model_id", "[Model ID]");

    sampleListModelEvaluations(project, modelId);
  }
}
