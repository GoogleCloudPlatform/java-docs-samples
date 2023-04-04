/*
 * Copyright 2023 Google LLC
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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StackdriverLoggingTest {
  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log
  // records!)
  private static final Logger logger = Logger.getLogger(StackdriverLogging.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void stackdriverLogging_shouldPrintOutUserSuppliedName() throws Exception {
    String userSupplied = "{\"name\":\"data\"}";
    String userSuppliedEncoded = Base64.getEncoder().encodeToString(userSupplied.getBytes());
    String messageJson = String.format("{\"message\":{\"data\": \"%s\"}}", userSuppliedEncoded);

    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.pubsub.topic.v1.messagePublished")
        .withData(messageJson.getBytes())
        .build();

    new StackdriverLogging().accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("Hello, data!");
  }

  @Test
  public void stackdriverLogging_shouldPrintOutWorldWhenNoData() throws Exception {
    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.pubsub.topic.v1.messagePublished")
        .build();

    new StackdriverLogging().accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("Hello, World!");
  }

  @Test
  public void stackdriverLogging_shouldPrintOutWorldWhenNoName() throws Exception {
    String userSupplied = "{\"test\":\"test\"}";
    String userSuppliedEncoded = Base64.getEncoder().encodeToString(userSupplied.getBytes());
    String messageJson = String.format("{\"message\":{\"data\": \"%s\"}}", userSuppliedEncoded);

    CloudEvent event = CloudEventBuilder.v1()
        .withId("0")
        .withSubject("test subject")
        .withSource(URI.create("https://example.com"))
        .withType("google.cloud.pubsub.topic.v1.messagePublished")
        .withData(messageJson.getBytes())
        .build();

    new StackdriverLogging().accept(event);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(logMessage).isEqualTo("Hello, World!");
  }
}
