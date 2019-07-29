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
// DO NOT EDIT! This is a generated sample ("LongRunningRequestAsync",  "automl_video_object_tracking_import_data")
// sample-metadata:
//   title: Import Data
//   description: Import training data into dataset
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingImportData [--args='[--dataset_id "[Dataset ID]"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.ImportDataRequest;
import com.google.cloud.automl.v1beta1.InputConfig;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingImportData {
  // [START automl_video_object_tracking_import_data]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.DatasetName;
   * import com.google.cloud.automl.v1beta1.GcsSource;
   * import com.google.cloud.automl.v1beta1.ImportDataRequest;
   * import com.google.cloud.automl.v1beta1.InputConfig;
   * import com.google.protobuf.Empty;
   * import java.util.Arrays;
   * import java.util.List;
   */

  /**
   * Import training data into dataset
   *
   * @param datasetId Dataset ID, e.g. VOT1234567890123456789
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleImportData(String datasetId, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // datasetId = "[Dataset ID]";
      // project = "[Google Cloud Project ID]";
      DatasetName name = DatasetName.of(project, "us-central1", datasetId);

      // Paths to CSV files stored in Cloud Storage with training data.
      // See "Preparing your training data" for more information.
      // https://cloud.google.com/video-intelligence/automl/object-tracking/docs/prepare
      String inputUrisElement = "gs://automl-video-datasets/youtube_8m_videos_animal_tiny.csv";
      List<String> inputUris = Arrays.asList(inputUrisElement);
      GcsSource gcsSource = GcsSource.newBuilder().addAllInputUris(inputUris).build();
      InputConfig inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();
      ImportDataRequest request =
          ImportDataRequest.newBuilder()
              .setName(name.toString())
              .setInputConfig(inputConfig)
              .build();
      autoMlClient.importDataAsync(request).get();
      System.out.println("Imported training data.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_import_data]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("dataset_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String datasetId = cl.getOptionValue("dataset_id", "[Dataset ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleImportData(datasetId, project);
  }
}
