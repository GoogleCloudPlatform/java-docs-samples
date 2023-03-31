package com.example.datacatalog;

import static org.awaitility.Awaitility.with;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;

import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.datacatalog.v1.DataCatalogClient;
import com.google.cloud.datacatalog.v1.DataCatalogSettings;
import com.google.cloud.datacatalog.v1.ImportEntriesMetadata;
import com.google.cloud.datacatalog.v1.ImportEntriesMetadata.ImportState;
import com.google.cloud.datacatalog.v1.ImportEntriesResponse;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsClient;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.EvaluatedCondition;
import org.threeten.bp.Duration;


// Sample to poll long-running operation for the state of entries import.

public class GetImportEntriesState {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String longRunningOperationName = "projects/my-project/locations/us-central1/operations/import_entries_abc";
    queryImportEntriesState(longRunningOperationName);
  }

  public static void queryImportEntriesState(String longRunningOperationName)
      throws IOException {

    try (DataCatalogClient dataCatalogClient = createDataCatalogClient()) {
      OperationsClient operationsClient = dataCatalogClient.getOperationsClient();

      // Periodically poll long-running operation to check state of the metadata import.
      Operation result =
          with()
              .pollInterval(fibonacci(TimeUnit.MINUTES))
              .await()
              .atMost(java.time.Duration.ofHours(1))
              .conditionEvaluationListener(GetImportEntriesState::printCondition)
              .until(() -> operationsClient.getOperation(longRunningOperationName),
                  Operation::getDone);

      // Interpret operation result.
      // It might result in error.
      if (result.hasError()) {
        System.out.println("Import failed: " + result.getError());
      }

      // If there were no fatal errors, operation will return ImportEntriesResponse, just like normal API call would.
      // Response contains useful statistics.
      if (result.hasResponse()) {
        ImportEntriesResponse response =
            ImportEntriesResponse.parseFrom(result.getResponse().getValue());
        System.out.println("Operation resolved in response: " + response);
      }

      // Operation metadata is also available to check. It contains a state of operation and partial errors, if any.
      ImportEntriesMetadata importEntriesMetadata = ImportEntriesMetadata.parseFrom(
          result.getMetadata().getValue());
      System.out.println("Operation metadata: " + importEntriesMetadata);
    }
  }

  private static void printCondition(EvaluatedCondition<Operation> condition) {
    ImportState state;
    try {
      ImportEntriesMetadata importEntriesMetadata = ImportEntriesMetadata.parseFrom(
          condition.getValue().getMetadata().getValue());
      state = importEntriesMetadata.getState();
    } catch (InvalidProtocolBufferException e) {
      state = ImportState.UNRECOGNIZED;
    }
    Duration duration = Duration.ofMillis(condition.getElapsedTimeInMS());

    System.out.println("Import Entries state after " + duration + ": " + state);

  }

  private static DataCatalogClient createDataCatalogClient() throws IOException {
    // It’s essential to provide RetrySettings to DataCatalogClient to enable blocking wait for the import result.
    RetrySettings retrySettings = RetrySettings.newBuilder()
        .setInitialRetryDelay(Duration.ofSeconds(1))
        .setRetryDelayMultiplier(1.5)
        .setMaxRetryDelay(Duration.ofMinutes(5))
        .setInitialRpcTimeout(Duration.ZERO)
        .setRpcTimeoutMultiplier(1.0)
        .setMaxRpcTimeout(Duration.ZERO)
        .setTotalTimeout(Duration.ofHours(4)) // set total polling timeout to 4 hours
        .build();
    DataCatalogSettings.Builder dcSettingsBuilder = DataCatalogSettings.newBuilder();
    dcSettingsBuilder.importEntriesOperationSettings()
        .setPollingAlgorithm(OperationTimedPollAlgorithm.create(retrySettings));
    dcSettingsBuilder.importEntriesSettings().setRetrySettings(retrySettings);
    return DataCatalogClient.create(dcSettingsBuilder.build());
  }

}
