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

package com.example.functions.helloworld;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.example.functions.helloworld.eventpojos.MockContext;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class GroovyTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private static final Logger BACKGROUND_LOGGER = Logger.getLogger(
      GroovyHelloBackground.class.getName());
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @BeforeClass
  public static void beforeClass() {
    BACKGROUND_LOGGER.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    request = PowerMockito.mock(HttpRequest.class);
    response = PowerMockito.mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    PowerMockito.when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    PowerMockito.when(response.getWriter()).thenReturn(writerOut);

    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.flush();
  }

  @Test
  public void functionsHelloworldGetGroovy_shouldPrintHelloWorld() throws IOException {
    new GroovyHelloWorld().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello World!");
  }

  @Test
  public void functionsHelloworldBackgroundGroovy_shouldPrintName() throws Exception {
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("John"));

    new GroovyHelloBackground().accept(request, new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(message);
  }

  @Test
  public void functionsHelloworldBackgroundGroovy_shouldPrintHelloWorld() throws Exception {
    new GroovyHelloBackground().accept(request, new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(message);
  }
}
