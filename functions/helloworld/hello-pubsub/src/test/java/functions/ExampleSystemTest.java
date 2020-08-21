/*
 * Copyright 2020 Google LLC
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

package functions;

// [START functions_pubsub_system_test]
import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
  private static final String TOPIC_NAME = System.getenv("FUNCTIONS_SYSTEM_TEST_TOPIC");
  private static final String FUNCTION_DEPLOYED_NAME = "HelloPubSub";

  private static Logging loggingClient;

  private static Publisher publisher;

  private HelloPubSub sampleUnderTest;

  @BeforeClass
  public static void setUp() throws IOException {
    loggingClient = LoggingOptions.getDefaultInstance().getService();
    publisher = Publisher.newBuilder(
        ProjectTopicName.of(PROJECT_ID, TOPIC_NAME)).build();
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
            Logging.EntryListOption.sortOrder(
                Logging.SortingField.TIMESTAMP, Logging.SortingOrder.DESCENDING)
        );

    // Serialize Stackdriver logging entries + collect them into a single string
    String logsConcat = StreamSupport.stream(logEntries.getValues().spliterator(), false)
        .map((x) -> x.toString())
        .collect(Collectors.joining("%n"));

    return logsConcat;
  }

  @Test
  public void helloPubSub_shouldRunOnGcf() throws Exception {
    String name = UUID.randomUUID().toString();

    // Subtract time to work-around local-GCF clock difference
    Instant startInstant = Instant.now().minus(Duration.ofMinutes(4));
    String startTimestamp = DateTimeFormatter.ISO_INSTANT.format(startInstant);

    // Publish to pub/sub topic
    ByteString byteStr = ByteString.copyFrom(name, StandardCharsets.UTF_8);
    PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();
    publisher.publish(pubsubApiMessage).get();

    // Keep retrying until the logs contain the desired invocation's log entry
    // (If the invocation failed, the retry process will eventually time out)
    RetryRegistry registry = RetryRegistry.of(RetryConfig.custom()
        .maxAttempts(8)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2))
        .retryOnResult(s -> !s.toString().contains(name))
        .build());
    Retry retry = registry.retry(name);
    String logEntry = Retry
        .decorateFunction(retry, ExampleSystemTest::getLogEntriesAsString)
        .apply(startTimestamp);

    // Perform final assertion (to make sure we fail on timeout)
    assertThat(logEntry).contains(name);
  }
}
// [END functions_pubsub_system_test]
