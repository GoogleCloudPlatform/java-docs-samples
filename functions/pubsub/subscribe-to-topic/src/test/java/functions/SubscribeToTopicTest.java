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
import functions.eventpojos.PubSubMessage;
import java.nio.charset.StandardCharsets;
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
    String encodedData = Base64.getEncoder().encodeToString(
        "hello".getBytes(StandardCharsets.UTF_8));

    PubSubMessage message = new PubSubMessage();
    message.setData(encodedData);

    new SubscribeToTopic().accept(message, null);

    assertThat("hello").isEqualTo(logHandler.getStoredLogRecords().get(0).getMessage());
  }
}
