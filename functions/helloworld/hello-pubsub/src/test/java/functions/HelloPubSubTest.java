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

// [START functions_pubsub_unit_test]

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import functions.eventpojos.PubSubMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for main.java.com.example.functions.helloworld.HelloPubSub.
 */
@RunWith(JUnit4.class)
public class HelloPubSubTest {

  private HelloPubSub sampleUnderTest;
  private static final Logger logger = Logger.getLogger(HelloPubSub.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @Before
  public void setUp() {
    sampleUnderTest = new HelloPubSub();
    logger.addHandler(LOG_HANDLER);
    LOG_HANDLER.clear();
  }

  @Test
  public void helloPubSub_shouldPrintName() {
    PubSubMessage pubSubMessage = new PubSubMessage();
    pubSubMessage.setData(Base64.getEncoder().encodeToString(
        "John".getBytes(StandardCharsets.UTF_8)));
    sampleUnderTest.accept(pubSubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(logMessage);
  }

  @Test
  public void helloPubSub_shouldPrintHelloWorld() {
    PubSubMessage pubSubMessage = new PubSubMessage();
    sampleUnderTest.accept(pubSubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(logMessage);
  }
}
// [END functions_pubsub_unit_test]
