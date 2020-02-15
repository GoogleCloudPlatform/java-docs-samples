/*
 * Copyright 2019 Google LLC
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

// [START functions_pubsub_unit_test]

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

// [END functions_pubsub_unit_test]

/**
 * Unit tests for {@link HelloPubSub}.
 */
// [START functions_pubsub_unit_test]
@RunWith(PowerMockRunner.class)
@PrepareForTest(HelloPubSub.class)
public class HelloPubSubIT {

  private HelloPubSub sampleUnderTest;
  private Logger logger;

  public final TestLogHandler logHandler = new TestLogHandler();

  @Before
  public void setUp() throws Exception {
    sampleUnderTest = new HelloPubSub();

    logger = Logger.getLogger(HelloPubSub.class.getName());
    logger.addHandler(logHandler);
  }

  @After
  public void tearDown() throws Exception {
    //Mockito.reset();
  }

  @Test
  public void helloPubSub_shouldPrintName() throws Exception {
    PubSubMessage pubSubMessage = new PubSubMessage();
    pubSubMessage.data = Base64.getEncoder().encodeToString(
        "John".getBytes(StandardCharsets.UTF_8));
    sampleUnderTest.accept(pubSubMessage, null);

    String logMessage = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(logMessage);
  }

  @Test
  public void helloPubSub_shouldPrintHelloWorld() throws Exception {
    PubSubMessage pubSubMessage = new PubSubMessage();
    sampleUnderTest.accept(pubSubMessage, null);

    String logMessage = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(logMessage);
  }
}
// [END functions_pubsub_unit_test]
