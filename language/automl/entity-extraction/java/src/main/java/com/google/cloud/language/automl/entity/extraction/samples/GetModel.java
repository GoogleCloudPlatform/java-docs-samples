package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_get_model]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.ModelName;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class GetModel {

  // Get a given model
  static void getModel(String projectId, String computeRegion, String modelId) throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String modelId = "YOUR_MODEL_ID";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the full path of the model.
    ModelName modelFullId = ModelName.of(projectId, computeRegion, modelId);

    // Get complete detail of the model.
    Model model = client.getModel(modelFullId);

    // Display the model information.
    System.out.println(String.format("Model name: %s", model.getName()));
    System.out.println(
        String.format(
            "Model Id: %s", model.getName().split("/")[model.getName().split("/").length - 1]));
    System.out.println(String.format("Model display name: %s", model.getDisplayName()));
    System.out.println(String.format("Dataset Id: %s", model.getDatasetId()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(model.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Model create time: %s", createTime));
    System.out.println(String.format("Model deployment state: %s", model.getDeploymentState()));
  }
}
// [END automl_natural_language_entity_get_model]
