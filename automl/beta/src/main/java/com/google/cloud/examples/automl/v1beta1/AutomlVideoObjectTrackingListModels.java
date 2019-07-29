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
// DO NOT EDIT! This is a generated sample ("RequestPaged",  "automl_video_object_tracking_list_models")
// sample-metadata:
//   title: List Models
//   description: List models and print details of each dataset
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingListModels [--args='[--filter "video_object_tracking_model_metadata:*"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ListModelsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingListModels {
  // [START automl_video_object_tracking_list_models]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.ListModelsRequest;
   * import com.google.cloud.automl.v1beta1.LocationName;
   * import com.google.cloud.automl.v1beta1.Model;
   */

  /**
   * List models and print details of each dataset
   *
   * @param filter An expression for filtering the results of the request. This filters for Models
   *     which have video_object_tracking_model_metadata.
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleListModels(String filter, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // filter = "video_object_tracking_model_metadata:*";
      // project = "[Google Cloud Project ID]";
      LocationName parent = LocationName.of(project, "us-central1");
      ListModelsRequest request =
          ListModelsRequest.newBuilder().setParent(parent.toString()).setFilter(filter).build();
      for (Model responseItem : autoMlClient.listModels(request).iterateAll()) {
        Model model = responseItem;
        // Print out the full name of the created model.
        //
        // This will have the format:
        //   projects/[Google Cloud Project Number]/locations/us-central1/models/VOT1234567890123456789
        //
        // The Model ID is the generated identifer in this path, e.g. VOT1234567890123456789
        // You will need this ID to perform operations on the model including predictions.
        //
        System.out.printf("Model name: %s\n", model.getName());
        // Print out the Display Name (the text you provided during creation)
        System.out.printf("Display name: %s\n", model.getDisplayName());
        // Print out the ID of the dataset used to create this model.
        //
        // Note: this is the Dataset ID, e.g. VOT1234567890123456789
        //
        System.out.printf("Dataset ID: %s\n", model.getDatasetId());
        System.out.printf("Create time: %s\n", model.getCreateTime());
        System.out.printf("Update time: %s\n", model.getUpdateTime());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_list_models]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("filter").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String filter = cl.getOptionValue("filter", "video_object_tracking_model_metadata:*");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleListModels(filter, project);
  }
}
