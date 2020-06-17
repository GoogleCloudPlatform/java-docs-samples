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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import functions.eventpojos.PubSubMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RetryPubSubTest {
  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(RetryPubSub.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test(expected = RuntimeException.class)
  public void retryPubsub_handlesRetryMsg() throws IOException {
    String data = gson.toJson(Map.of("retry", true));
    String encodedData = new String(
        Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(encodedData);

    new RetryPubSub().accept(pubsubMessage, null);
  }

  @Test
  public void retryPubsub_handlesStopMsg() throws IOException {
    String data = gson.toJson(Map.of("retry", false));
    String encodedData = new String(
        Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(encodedData);

    new RetryPubSub().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Not retrying...").isEqualTo(logMessage);
  }

  @Test
  public void retryPubsub_handlesEmptyMsg() throws IOException {
    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData("");

    new RetryPubSub().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Not retrying...").isEqualTo(logMessage);
  }
}
