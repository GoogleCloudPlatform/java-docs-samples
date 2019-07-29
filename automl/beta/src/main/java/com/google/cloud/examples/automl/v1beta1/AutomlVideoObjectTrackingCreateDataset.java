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
// DO NOT EDIT! This is a generated sample ("Request",  "automl_video_object_tracking_create_dataset")
// sample-metadata:
//   title: Create Dataset
//   description: Create Dataset
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingCreateDataset [--args='[--display_name "My_Dataset_Name_123"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.CreateDatasetRequest;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.VideoObjectTrackingDatasetMetadata;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingCreateDataset {
  // [START automl_video_object_tracking_create_dataset]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.CreateDatasetRequest;
   * import com.google.cloud.automl.v1beta1.Dataset;
   * import com.google.cloud.automl.v1beta1.LocationName;
   * import com.google.cloud.automl.v1beta1.VideoObjectTrackingDatasetMetadata;
   */

  /**
   * Create Dataset
   *
   * @param displayName The name of the dataset to show in the interface. The name can be up to 32
   *     characters long and can consist only of ASCII Latin letters A-Z and a-z, underscores (_),
   *     and ASCII digits 0-9. Must be unique within the scope of the provided GCP Project and
   *     Location.
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleCreateDataset(String displayName, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // displayName = "My_Dataset_Name_123";
      // project = "[Google Cloud Project ID]";
      LocationName parent = LocationName.of(project, "us-central1");

      // User-provided description of dataset (optional)
      String description = "Description of this dataset";

      // Initialized video_object_tracking_dataset_metadata field must be provided.
      // This specifies this Dataset is to be used for video object tracking.
      VideoObjectTrackingDatasetMetadata videoObjectTrackingDatasetMetadata =
          VideoObjectTrackingDatasetMetadata.newBuilder().build();
      Dataset dataset =
          Dataset.newBuilder()
              .setDisplayName(displayName)
              .setDescription(description)
              .setVideoObjectTrackingDatasetMetadata(videoObjectTrackingDatasetMetadata)
              .build();
      CreateDatasetRequest request =
          CreateDatasetRequest.newBuilder()
              .setParent(parent.toString())
              .setDataset(dataset)
              .build();
      Dataset response = autoMlClient.createDataset(request);
      System.out.println("Created Dataset.");
      Dataset dataset = response;
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
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_create_dataset]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("display_name").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String displayName = cl.getOptionValue("display_name", "My_Dataset_Name_123");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleCreateDataset(displayName, project);
  }
}
