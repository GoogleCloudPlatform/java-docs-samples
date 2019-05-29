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
 * Unit tests for {@link HelloGcsSample}.
 */
@RunWith(PowerMockRunner.class)
public class HelloGcsSampleTest {


  private HelloGcsSample sampleUnderTest;
  @Mock private Logger loggerInstance;

  @Before
  public void setUp() throws Exception {
    loggerInstance = mock(Logger.class);
    PowerMockito.mockStatic(Logger.class);

    Mockito
        .when(Logger.getLogger(HelloGcsSample.class.getName()))
        .thenReturn(loggerInstance);

    sampleUnderTest = new HelloGcsSample();
  }

  @After
  public void tearDown() throws Exception {
    Mockito.reset();
  }

  @PrepareForTest({Logger.class, HelloGcsSample.class})
  @Test
  public void helloGcs_shouldPrintUploadedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "foo.txt";
    event.metageneration = "1";
    sampleUnderTest.helloGcs(event);
    verify(loggerInstance, times(1)).info("File foo.txt uploaded.");
  }

  @PrepareForTest({Logger.class, HelloGcsSample.class})
  @Test
  public void helloGcs_shouldPrintMetadataUpdatedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "baz.txt";
    event.metageneration = "2";
    sampleUnderTest.helloGcs(event);
    verify(loggerInstance, times(1)).info("File baz.txt metadata updated.");
  }


}
