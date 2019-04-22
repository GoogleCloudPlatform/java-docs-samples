package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_get_operation_status]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.longrunning.Operation;
import java.io.IOException;

class GetOperationStatus {

  // Get the status of a given operation
  static void getOperationStatus(String operationFullId) throws IOException {
    // String operationFullId = "COMPLETE_NAME_OF_OPERATION";
    // like: projects/[projectId]/locations/us-central1/operations/[operationId]

    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the latest state of a long-running operation.
    Operation response = client.getOperationsClient().getOperation(operationFullId);

    // Display operation details.
    System.out.println(String.format("Operation details:"));
    System.out.println(String.format("\tName: %s", response.getName()));
    System.out.println(String.format("\tMetadata:"));
    System.out.println(String.format("\t\tType Url: %s", response.getMetadata().getTypeUrl()));
    System.out.println(
        String.format(
            "\t\tValue: %s", response.getMetadata().getValue().toStringUtf8().replace("\n", "")));
    System.out.println(String.format("\tDone: %s", response.getDone()));
    if (response.hasResponse()) {
      System.out.println("\tResponse:");
      System.out.println(String.format("\t\tType Url: %s", response.getResponse().getTypeUrl()));
      System.out.println(
          String.format(
              "\t\tValue: %s", response.getResponse().getValue().toStringUtf8().replace("\n", "")));
    }
    if (response.hasError()) {
      System.out.println("\tResponse:");
      System.out.println(String.format("\t\tError code: %s", response.getError().getCode()));
      System.out.println(String.format("\t\tError message: %s", response.getError().getMessage()));
    }
  }
}
// [END automl_natural_language_entity_get_operation_status]
