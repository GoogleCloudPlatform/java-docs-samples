package functions;

// [START functions_storage_system_test]
import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExampleSystemTest {

  // TODO<developer>: set these values (as environment variables)
  private static final String PROJECT_ID = System.getenv("GCP_PROJECT");
  private static final String FUNCTIONS_BUCKET = System.getenv("FUNCTIONS_BUCKET");
  private static final String FUNCTION_DEPLOYED_NAME = "HelloGcs";
  private static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();

  private static Logging loggingClient;

  private HelloGcs sampleUnderTest;

  @BeforeClass
  public static void setUp() throws IOException {
    loggingClient = LoggingOptions.getDefaultInstance().getService();
  }

  private static String getLogEntriesAsString(String startTimestamp) {
    // Construct Stackdriver logging filter
    // See this page for more info: https://cloud.google.com/logging/docs/view/advanced-queries
    String filter = "resource.type=\"cloud_function\""
        + " AND severity=INFO"
        + " AND resource.labels.function_name=" + FUNCTION_DEPLOYED_NAME
        + String.format(" AND timestamp>=\"%s\"", startTimestamp);

    // Get Stackdriver logging entries
    Page<LogEntry> logEntries =
        loggingClient.listLogEntries(
            Logging.EntryListOption.filter(filter),
            Logging.EntryListOption.sortOrder(Logging.SortingField.TIMESTAMP, Logging.SortingOrder.DESCENDING)
        );

    // Serialize Stackdriver logging entries + collect them into a single string
    String logsConcat = StreamSupport.stream(logEntries.getValues().spliterator(), false)
        .map((x) -> x.toString())
        .collect(Collectors.joining("%n"));

    return logsConcat;
  }

  @Test
  public void helloGcs_shouldRunOnGcf() {
    String filename = String.format("test-%s.txt", UUID.randomUUID());
    String expected = String.format("File %s uploaded.", filename);

    // Subtract time to work-around local-GCF clock difference
    Instant startInstant = Instant.now().minus(Duration.ofMinutes(4));
    String startTimestamp = DateTimeFormatter.ISO_INSTANT.format(startInstant);

    // Upload a file to Cloud Storage
    BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(FUNCTIONS_BUCKET, filename)).build();
    STORAGE.create(blobInfo);

    // Keep retrying until the logs contain the desired invocation's log entry
    // (If the invocation failed, the retry process will eventually time out)
    RetryRegistry registry = RetryRegistry.of(RetryConfig.custom()
        .maxAttempts(8)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2))
        .retryOnResult(s -> !s.toString().contains(filename))
        .build());
    Retry retry = registry.retry(filename);
    String logEntry = Retry
        .decorateFunction(retry, ExampleSystemTest::getLogEntriesAsString)
        .apply(startTimestamp);

    // Perform final assertion (to make sure we fail on timeout)
    assertThat(logEntry).contains(filename);
  }
}
// [END functions_storage_system_test]