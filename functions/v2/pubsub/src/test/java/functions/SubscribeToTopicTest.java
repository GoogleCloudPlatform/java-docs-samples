/*
 * Copyright 2021 Google LLC
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SubscribeToTopicTest {
  private static final Logger logger = Logger.getLogger(SubscribeToTopic.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(logHandler);
  }

  @Test
  public void functionsPubsubSubscribe_shouldPrintPubsubMessage() throws Exception {
    String msg = "Hello World";
    String encodedMessage = Base64.getEncoder().encodeToString(msg.getBytes());
    String encodedData = new String("{\"message\": { \"data\": \"" + encodedMessage + "\"} }");

    CloudEvent event =
        CloudEventBuilder.v1()
            .withId("0")
            .withType("pubsub.message")
            .withSource(URI.create("https://example.com"))
            .withData(encodedData.getBytes())
            .build();

    new SubscribeToTopic().accept(event);

    assertThat("Hello, " + msg + "!").isEqualTo(
        logHandler.getStoredLogRecords().get(0).getMessage());
  }
}
