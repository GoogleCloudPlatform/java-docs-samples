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
import functions.eventpojos.MockContext;
import functions.eventpojos.PubSubMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GroovyHelloPubSubTest {
  private static final Logger logger = Logger.getLogger(
      GroovyHelloPubSub.class.getName());
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.flush();
  }

  @Test
  public void functionsHelloworldPubsubGroovy_shouldPrintName() throws Exception {
    PubSubMessage message = new PubSubMessage();
    message.setData(Base64.getEncoder().encodeToString(
        "John".getBytes(StandardCharsets.UTF_8)));

    new GroovyHelloPubSub().accept(message, new MockContext());

    assertThat("Hello John!").isEqualTo(
        LOG_HANDLER.getStoredLogRecords().get(0).getMessage());
  }

  @Test
  public void functionsHelloworldPubsubGroovy_shouldPrintHelloWorld() throws Exception {
    new GroovyHelloPubSub().accept(new PubSubMessage(), new MockContext());

    assertThat("Hello world!").isEqualTo(
        LOG_HANDLER.getStoredLogRecords().get(0).getMessage());
  }
}
