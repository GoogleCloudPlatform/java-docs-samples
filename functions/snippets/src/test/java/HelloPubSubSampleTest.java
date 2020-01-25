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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.logging.Logger;

import com.google.common.primitives.Chars;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

// [END functions_pubsub_unit_test]

/**
 * Unit tests for {@link HelloPubSubSample}.
 */
// [START functions_pubsub_unit_test]
@RunWith(PowerMockRunner.class)
@PrepareForTest({Logger.class, HelloPubSubSample.class})
public class HelloPubSubSampleTest {

  private HelloPubSubSample sampleUnderTest;
  @Mock private static Logger loggerInstance;

  @Before
  public void setUp() throws Exception {
    PowerMockito.mockStatic(Logger.class);

    if (loggerInstance == null) {
      loggerInstance = mock(Logger.class);
    }

    Mockito
        .when(Logger.getLogger(HelloPubSubSample.class.getName()))
        .thenReturn(loggerInstance);

    sampleUnderTest = new HelloPubSubSample();
  }

  @After
  public void tearDown() throws Exception {
    //Mockito.reset();
  }

  @Test
  public void helloPubSub_shouldPrintName() throws Exception {
    PubSubMessage message = new PubSubMessage();
    message.data = Base64.getEncoder().encodeToString("John".getBytes(Charset.defaultCharset()));
    sampleUnderTest.accept(message, null);
    verify(loggerInstance, times(1)).info("Hello John!");
  }

  @Test
  public void helloPubSub_shouldPrintHelloWorld() throws Exception {
    PubSubMessage message = new PubSubMessage();
    sampleUnderTest.accept(message, null);
    verify(loggerInstance, times(1)).info("Hello world!");
  }
}
// [END functions_pubsub_unit_test]
