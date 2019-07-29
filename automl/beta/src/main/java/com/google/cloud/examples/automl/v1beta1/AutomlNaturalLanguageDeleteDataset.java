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
// DO NOT EDIT! This is a generated sample ("LongRunningRequestAsync",  "automl_natural_language_delete_dataset")
// sample-metadata:
//   title: Delete Dataset
//   description: Delete Dataset
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlNaturalLanguageDeleteDataset [--args='[--dataset_id "[Dataset ID]"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.DeleteDatasetRequest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlNaturalLanguageDeleteDataset {
  // [START automl_natural_language_delete_dataset]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.DatasetName;
   * import com.google.cloud.automl.v1beta1.DeleteDatasetRequest;
   * import com.google.protobuf.Empty;
   */

  /**
   * Delete Dataset
   *
   * @param datasetId Dataset ID, e.g. VOT1234567890123456789
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleDeleteDataset(String datasetId, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // datasetId = "[Dataset ID]";
      // project = "[Google Cloud Project ID]";
      DatasetName name = DatasetName.of(project, "us-central1", datasetId);
      DeleteDatasetRequest request =
          DeleteDatasetRequest.newBuilder().setName(name.toString()).build();
      autoMlClient.deleteDatasetAsync(request).get();
      System.out.println("Deleted Dataset.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_natural_language_delete_dataset]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("dataset_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String datasetId = cl.getOptionValue("dataset_id", "[Dataset ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleDeleteDataset(datasetId, project);
  }
}
