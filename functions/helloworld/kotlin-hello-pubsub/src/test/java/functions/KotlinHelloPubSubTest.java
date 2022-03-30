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
import com.google.events.cloud.pubsub.v1.Message;
import functions.eventpojos.MockContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KotlinHelloPubSubTest {
  private static final Logger logger = Logger.getLogger(
      HelloPubSub.class.getName());
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
  public void functionsHelloworldPubSubKotlin_shouldPrintName() throws Exception {
    Message pubSubMessage = new Message();
    pubSubMessage.setData(Base64.getEncoder().encodeToString(
        "John".getBytes(StandardCharsets.UTF_8)));

    new HelloPubSub().accept(pubSubMessage, new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(message);
  }

  @Test
  public void functionsHelloworldPubSubKotlin_shouldPrintHelloWorld() throws Exception {
    new HelloPubSub().accept(new Message(), new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(message);
  }
}
