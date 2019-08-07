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
// DO NOT EDIT! This is a generated sample ("LongRunningStartThenCancel",  "automl_video_object_tracking_create_model")
// sample-metadata:
//   title: Create Model
//   description: Create a model
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingCreateModel [--args='[--display_name "My_Model_Name_123"] [--dataset_id "[Dataset ID]"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.CreateModelRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.VideoObjectTrackingModelMetadata;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingCreateModel {
  // [START automl_video_object_tracking_create_model]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.api.gax.longrunning.OperationFuture;
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.CreateModelRequest;
   * import com.google.cloud.automl.v1beta1.LocationName;
   * import com.google.cloud.automl.v1beta1.Model;
   * import com.google.cloud.automl.v1beta1.OperationMetadata;
   * import com.google.cloud.automl.v1beta1.VideoObjectTrackingModelMetadata;
   */

  /**
   * Create a model
   *
   * @param displayName The name of the model to show in the interface. The name can be up to 32
   *     characters long and can consist only of ASCII Latin letters A-Z and a-z, underscores (_),
   *     and ASCII digits 0-9. Must be unique within the scope of the provided GCP Project and
   *     Location.
   * @param datasetId Required. The resource ID of the dataset used to create the model. The dataset
   *     must come from the same ancestor project and location.
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleCreateModel(String displayName, String datasetId, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // displayName = "My_Model_Name_123";
      // datasetId = "[Dataset ID]";
      // project = "[Google Cloud Project ID]";
      LocationName parent = LocationName.of(project, "us-central1");

      // Initialized video_object_tracking_model_metadata field must be provided.
      // This specifies this Dataset is to be used for video object tracking.
      VideoObjectTrackingModelMetadata videoObjectTrackingModelMetadata =
          VideoObjectTrackingModelMetadata.newBuilder().build();
      Model model =
          Model.newBuilder()
              .setDisplayName(displayName)
              .setDatasetId(datasetId)
              .setVideoObjectTrackingModelMetadata(videoObjectTrackingModelMetadata)
              .build();
      CreateModelRequest request =
          CreateModelRequest.newBuilder().setParent(parent.toString()).setModel(model).build();
      OperationFuture<Model, OperationMetadata> operation =
          autoMlClient.createModelOperationCallable().futureCall(request);

      // The long-running operation has started.

      // Store the operation name to poll for operation status:
      String operationName = operation.getName();
      System.out.printf("Started long-running operation: %s", operationName);
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_create_model]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("display_name").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("dataset_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String displayName = cl.getOptionValue("display_name", "My_Model_Name_123");
    String datasetId = cl.getOptionValue("dataset_id", "[Dataset ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleCreateModel(displayName, datasetId, project);
  }
}
