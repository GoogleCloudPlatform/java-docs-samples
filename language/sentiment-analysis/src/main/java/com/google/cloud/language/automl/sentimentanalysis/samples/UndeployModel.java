/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.language.automl.sentimentanalysis.samples;

// [START automl_natural_language_sentiment_undeploy_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.UndeployModelRequest;
import com.google.protobuf.Empty;

class UndeployModel {

  // Undeploy a given model
  public static void undeployModel(String projectId, String computeRegion, String modelId)
      throws Exception {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "us-central1";
    // String modelId = "YOUR_MODEL_ID";

    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the full path of the model.
      ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

      // Build undeploy model request.
      UndeployModelRequest undeployModelRequest =
          UndeployModelRequest.newBuilder().setName(modelName.toString()).build();

      // Undeploy a model with the undeploy model request.
      OperationFuture<Empty, OperationMetadata> response =
          client.undeployModelAsync(undeployModelRequest);

      // Display the undeployment details of model.
      System.out.println(String.format("Undeployment Details:"));
      System.out.println(String.format("\tName: %s", response.getName()));
      System.out.println(String.format("\tMetadata:"));
      System.out.println(response.getMetadata().toString());
    }
  }
}
// [END automl_natural_language_sentiment_undeploy_model]
