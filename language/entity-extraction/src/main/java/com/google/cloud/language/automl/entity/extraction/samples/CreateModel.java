package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_create_model]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.Model;
import com.google.cloud.automl.v1beta1.OperationMetadata;
import com.google.cloud.automl.v1beta1.TextExtractionModelMetadata;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

class CreateModel {

  // Create a model
  static void createModel(
      String projectId, String computeRegion, String datasetId, String modelName)
      throws IOException, InterruptedException, ExecutionException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String datasetId = "YOUR_DATASET_ID";
    // String modelName = "YOUR_MODEL_NAME";

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    // Set model meta data
    TextExtractionModelMetadata textExtractionModelMetadata =
        TextExtractionModelMetadata.newBuilder().build();

    // Set model name, dataset and metadata.
    Model myModel =
        Model.newBuilder()
            .setDisplayName(modelName)
            .setDatasetId(datasetId)
            .setTextExtractionModelMetadata(textExtractionModelMetadata)
            .build();

    // Create a model with the model metadata in the region.
    OperationFuture<Model, OperationMetadata> response =
        client.createModelAsync(projectLocation, myModel);

    System.out.println(
        String.format("Training operation name: %s", response.getInitialFuture().get().getName()));
    System.out.println("Training started...");
  }
}
// [END automl_natural_language_entity_create_model]
