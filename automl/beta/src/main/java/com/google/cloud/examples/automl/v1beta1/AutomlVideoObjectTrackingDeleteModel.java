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
// DO NOT EDIT! This is a generated sample ("LongRunningRequestAsync",  "automl_video_object_tracking_delete_model")
// sample-metadata:
//   title: Delete Model
//   description: Delete Model
//   usage: gradle run -PmainClass=com.google.cloud.examples.automl.v1beta1.AutomlVideoObjectTrackingDeleteModel [--args='[--model_id "[Model ID]"] [--project "[Google Cloud Project ID]"]']

package com.google.cloud.examples.automl.v1beta1;

import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DeleteModelRequest;
import com.google.cloud.automl.v1beta1.ModelName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AutomlVideoObjectTrackingDeleteModel {
  // [START automl_video_object_tracking_delete_model]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.automl.v1beta1.AutoMlClient;
   * import com.google.cloud.automl.v1beta1.DeleteModelRequest;
   * import com.google.cloud.automl.v1beta1.ModelName;
   * import com.google.protobuf.Empty;
   */

  /**
   * Delete Model
   *
   * @param modelId Model ID, e.g. VOT1234567890123456789
   * @param project Required. Your Google Cloud Project ID.
   */
  public static void sampleDeleteModel(String modelId, String project) {
    try (AutoMlClient autoMlClient = AutoMlClient.create()) {
      // modelId = "[Model ID]";
      // project = "[Google Cloud Project ID]";
      ModelName name = ModelName.of(project, "us-central1", modelId);
      DeleteModelRequest request = DeleteModelRequest.newBuilder().setName(name.toString()).build();
      autoMlClient.deleteModelAsync(request).get();
      System.out.println("Deleted Model.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END automl_video_object_tracking_delete_model]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("model_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("project").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String modelId = cl.getOptionValue("model_id", "[Model ID]");
    String project = cl.getOptionValue("project", "[Google Cloud Project ID]");

    sampleDeleteModel(modelId, project);
  }
}
