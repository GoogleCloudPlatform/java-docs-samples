package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_deploy_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.DeployModelRequest;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.protobuf.Empty;

class DeployModel {

  static void deployModel(String projectId, String computeRegion, String modelId) throws Exception {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String modelId = "MODEL_ID";

    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Build deploy model request.
    DeployModelRequest deployModelRequest =
        DeployModelRequest.newBuilder().setName(modelFullId.toString()).build();

    // Deploy a model with the deploy model request.
    OperationFuture<Empty, OperationMetadata> response =
        client.deployModelAsync(deployModelRequest);

    // Display the deployment details of model.
    System.out.println(String.format("Deployment Details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(String.format("\t\tType Url: %s", response.getMetadata()));
  }
}
// [END automl_natural_language_entity_deploy_model]
