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
package com.example.functions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for {@link HelloPubSubSample}.
 */
@RunWith(PowerMockRunner.class)
public class HelloBackgroundSampleTest {

  private HelloBackgroundSample sampleUnderTest;
  @Mock private Logger loggerInstance;

  @Before
  public void setUp() throws Exception {
    loggerInstance = mock(Logger.class);
    PowerMockito.mockStatic(Logger.class);

    Mockito
        .when(Logger.getLogger(HelloBackgroundSample.class.getName()))
        .thenReturn(loggerInstance);

    sampleUnderTest = new HelloBackgroundSample();
  }

  @After
  public void tearDown() throws Exception {
    Mockito.reset();
  }

  @PrepareForTest({Logger.class, HelloBackgroundSample.class})
  @Test
  public void helloBackground_shouldPrintName() throws Exception {
    BackgroundEvent event = new BackgroundEvent();
    event.name = "John";
    sampleUnderTest.helloBackground(event);
    verify(loggerInstance, times(1)).info("Hello John!");
  }

  @PrepareForTest({Logger.class, HelloBackgroundSample.class})
  @Test
  public void helloBackground_shouldPrintHelloWorld() throws Exception {
    BackgroundEvent event = new BackgroundEvent();
    sampleUnderTest.helloBackground(event);
    verify(loggerInstance, times(1)).info("Hello world!");
  }
}
// [END functions_pubsub_unit_test]
