package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_delete_model]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteModel {

  public static void deleteModel(String projectId, String computeRegion, String modelId)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String modelId = "YOUR_MODEL_ID";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Delete a model.
    Empty response = client.deleteModelAsync(modelFullId).get();

    System.out.println("Model deletion started...");
    System.out.println(String.format("Model deleted. %s", response));
  }
}
// [END automl_natural_language_entity_delete_model]
