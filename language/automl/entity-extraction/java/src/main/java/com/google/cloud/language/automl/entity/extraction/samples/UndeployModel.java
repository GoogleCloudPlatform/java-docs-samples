package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_undeploy_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.UndeployModelRequest;
import com.google.protobuf.Empty;

class UndeployModel {

  static void undeployModel(String projectId, String computeRegion, String modelId)
      throws Exception {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String modelId = "YOUR_MODEL_ID";

    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Build undeploy model request.
    UndeployModelRequest undeployModelRequest =
        UndeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

    // Undeploy a model with the undeploy model request.
    OperationFuture<Empty, OperationMetadata> response =
        client.undeployModelAsync(undeployModelRequest);

    // Display the undeployment details of model.
    System.out.println(String.format("Undeployment Details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(response.getMetadata().get().toString());
  }
}
// [END automl_natural_language_entity_undeploy_model]
