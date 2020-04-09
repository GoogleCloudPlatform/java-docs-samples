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

package com.example.functions.logging;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.example.functions.logging.eventpojos.PubSubMessage;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class LogsTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger STACKDRIVER_LOGGER = Logger.getLogger(
      StackdriverLogging.class.getName());

  private static final TestLogHandler logHandler = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @BeforeClass
  public static void beforeClass() {
    STACKDRIVER_LOGGER.addHandler(logHandler);
  }

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    PowerMockito.when(response.getWriter()).thenReturn(writerOut);

    logHandler.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    logHandler.clear();
  }

  @Test
  public void retrieveLogsTest() throws IOException {
    new RetrieveLogs().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Logs retrieved successfully.");
  }

  @Test
  public void stackdriverLogging() throws IOException {
    PubSubMessage pubsubMessage = gson.fromJson(
        "{\"data\":\"ZGF0YQ==\",\"messageId\":\"id\"}", PubSubMessage.class);
    new StackdriverLogging().accept(pubsubMessage, null);

    String logMessage = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello, data").isEqualTo(logMessage);
  }
}
