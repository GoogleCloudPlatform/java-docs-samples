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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.example.functions.helloworld.eventpojos.GcsEvent;
import com.example.functions.helloworld.eventpojos.MockContext;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.LogRecord;
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

@RunWith(JUnit4.class)
public class HelloWorldSnippetsTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger BACKGROUND_LOGGER = Logger.getLogger(HelloBackground.class.getName());
  private static final Logger PUBSUB_LOGGER = Logger.getLogger(HelloPubSub.class.getName());
  private static final Logger GCS_LOGGER = Logger.getLogger(HelloGcs.class.getName());
  private static final Logger GCS_GENERIC_LOGGER = Logger.getLogger(
      HelloGcsGeneric.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @BeforeClass
  public static void beforeClass() {
    BACKGROUND_LOGGER.addHandler(LOG_HANDLER);
    PUBSUB_LOGGER.addHandler(LOG_HANDLER);
    GCS_LOGGER.addHandler(LOG_HANDLER);
    GCS_GENERIC_LOGGER.addHandler(LOG_HANDLER);
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
    when(response.getWriter()).thenReturn(writerOut);

    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.clear();
  }

  @Test
  public void helloWorldTest() throws IOException {
    new HelloWorld().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello World!");
  }

  @Test
  public void functionsHelloworldStorageGeneric_shouldPrintEvent() throws IOException {
    GcsEvent event = new GcsEvent();
    event.setBucket("some-bucket");
    event.setName("some-file.txt");
    event.setTimeCreated(new Date());
    event.setUpdated(new Date());

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.metadataUpdate";

    new HelloGcsGeneric().accept(event, context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    assertThat(logs.get(1).getMessage()).isEqualTo(
        "Event Type: google.storage.object.metadataUpdate");
    assertThat(logs.get(2).getMessage()).isEqualTo("Bucket: some-bucket");
    assertThat(logs.get(3).getMessage()).isEqualTo("File: some-file.txt");
  }

  @Test
  public void helloHttp_noParamsGet() throws IOException {
    new HelloHttp().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).isEqualTo("Hello world!");
  }

  @Test
  public void helloHttp_urlParamsGet() throws IOException {
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("Tom"));

    new HelloHttp().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).isEqualTo("Hello Tom!");
  }

  @Test
  public void helloHttp_bodyParamsPost() throws IOException {
    BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));

    when(request.getReader()).thenReturn(jsonReader);

    new HelloHttp().service(request, response);
    writerOut.flush();

    assertThat(responseOut.toString()).isEqualTo("Hello Jane!");
  }
}
