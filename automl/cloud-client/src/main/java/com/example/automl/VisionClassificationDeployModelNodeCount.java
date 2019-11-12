package com.example.automl;

// [START automl_vision_classification_deploy_model_node_count]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1.AutoMlClient;
import com.google.cloud.automl.v1.DeployModelRequest;
import com.google.cloud.automl.v1.ImageClassificationModelDeploymentMetadata;
import com.google.cloud.automl.v1.ModelName;
import com.google.cloud.automl.v1.OperationMetadata;
import com.google.protobuf.Empty;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

class VisionClassificationDeployModelNodeCount {

  static void visionClassificationDeployModelNodeCount()
      throws InterruptedException, ExecutionException, IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String modelId = "YOUR_MODEL_ID";
    visionClassificationDeployModelNodeCount(projectId, modelId);
  }

  // Deploy a model for prediction with a specified node count (can be used to redeploy a model)
  static void visionClassificationDeployModelNodeCount(String projectId, String modelId)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the full path of the model.
      ModelName modelFullId = ModelName.of(projectId, "us-central1", modelId);
      ImageClassificationModelDeploymentMetadata metadata =
          ImageClassificationModelDeploymentMetadata.newBuilder().setNodeCount(2).build();
      DeployModelRequest request =
          DeployModelRequest.newBuilder()
              .setName(modelFullId.toString())
              .setImageClassificationModelDeploymentMetadata(metadata)
              .build();
      OperationFuture<Empty, OperationMetadata> future = client.deployModelAsync(request);

      future.get();
      System.out.println("Model deployment finished");
    }
  }
}
// [END automl_vision_classification_deploy_model_node_count]
