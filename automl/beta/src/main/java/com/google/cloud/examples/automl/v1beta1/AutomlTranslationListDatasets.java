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
// DO NOT EDIT! This is a generated sample ("RequestPaged",  "automl_translation_list_datasets")
// sample-metadata:
//   title: List Datasets
//   description: List datasets and print each details of each dataset
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlTranslationListDatasets [--args='[--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.automl.v1beta1.ListDatasetsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlTranslationListDatasets {
  // [START automl_translation_list_datasets]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.Dataset;
   * import com.google.cloud.automl.v1beta1.ListDatasetsRequest;
   * import com.google.cloud.automl.v1beta1.LocationName;
   */

  /**
   * List datasets and print each details of each dataset
   *
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleListDatasets(String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // project = "[Google Cloud Project ID]";
      LocationName parent = LocationName.of(project, "us-central1");

      // An expression for filtering the results of the request.
      // This filters for Datasets which have translation_dataset_metadata.
      String filter = "translation_dataset_metadata:*";
      ListDatasetsRequest request =
          ListDatasetsRequest.newBuilder().setParent(parent.toString()).setFilter(filter).build();
      for (Dataset responseItem : autoMlClient.listDatasets(request).iterateAll()) {
        Dataset dataset = responseItem;
        // Print out the full name of the created dataset.
        //
        // This will have the format:
        //   projects/[Google Cloud Project Number]/locations/us-central1/datasets/VOT1234567890123456789
        //
        // The Dataset ID is the generated identifer in this path, e.g. VOT1234567890123456789
        // You will need this ID to perform operations on the dataset as well as to create a model.
        //
        System.out.printf("Name: %s\n", dataset.getName());
        // Print out the Display Name (the text you provided during creation)
        System.out.printf("Display Name: %s\n", dataset.getDisplayName());
        // Print out the user-provided description (may be blank)
        System.out.printf("Description: %s\n", dataset.getDescription());
        // The number of examples in the dataset, if any.
        // Added by importing data via importData
        //
        System.out.printf("Example count: %s\n", dataset.getExampleCount());
        // Translation Dataset Metadata
        System.out.printf(
            "Source language code: %s\n",
            dataset.getTranslationDatasetMetadata().getSourceLanguageCode());
        System.out.printf(
            "Target language code: %s\n",
            dataset.getTranslationDatasetMetadata().getTargetLanguageCode());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_translation_list_datasets]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleListDatasets(project);
  }
}
