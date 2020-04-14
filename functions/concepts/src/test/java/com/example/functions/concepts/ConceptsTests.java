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

package com.example.functions.concepts;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.example.functions.concepts.eventpojos.PubSubMessage;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
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
public class ConceptsTests {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger RETRY_LOGGER = Logger.getLogger(RetryPubSub.class.getName());
  private static final Logger INFINITE_RETRY_LOGGER = Logger.getLogger(
      RetryTimeout.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private static final Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @BeforeClass
  public static void beforeClass() {
    RETRY_LOGGER.addHandler(LOG_HANDLER);
    INFINITE_RETRY_LOGGER.addHandler(LOG_HANDLER);
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

    LOG_HANDLER.clear();
  }

  @After
  public void afterTest() {
    System.out.flush();
    LOG_HANDLER.clear();
  }

  @Test
  public void scopesTest() throws IOException {
    new Scopes().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Instance:");
  }

  @Test
  public void lazyTest() throws IOException {
    new LazyFields().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Lazy global:");
  }

  @Test
  public void filesTest() throws IOException {
    new FileSystem().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Files:");
  }

  @Test
  public void envTest() throws IOException {
    environmentVariables.set("FOO", "BAR");
    new EnvVars().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("BAR");
  }

  @Test
  public void helloExecutionCount() throws IOException {
    new ExecutionCount().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Instance execution count: 1");
  }

  @Test(expected = RuntimeException.class)
  public void retryPubsub_handlesRetryMsg() throws IOException {
    String data = "{\"retry\": true}";
    String encodedData = new String(Base64.getEncoder().encode(data.getBytes()));

    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(encodedData);

    new RetryPubSub().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
  }

  @Test
  public void retryPubsub_handlesStopMsg() throws IOException {
    String data = "{\"retry\": false}";
    String encodedData = new String(Base64.getEncoder().encode(data.getBytes()));

    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(encodedData);

    new RetryPubSub().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Not retrying...").isEqualTo(logMessage);
  }

  @Test
  public void retryPubsub_handlesEmptyMsg() throws IOException {
    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData("");

    new RetryPubSub().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Not retrying...").isEqualTo(logMessage);
  }

  @Test
  public void infiniteRetries_handlesRetryMsg() throws IOException {
    String timestampData = String.format(
        "{\"timestamp\":\"%s\"}", ZonedDateTime.now(ZoneOffset.UTC).toString());

    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(timestampData);

    new RetryTimeout().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(String.format("Processing event %s.", timestampData)).isEqualTo(logMessage);
  }

  @Test
  public void infiniteRetries_handlesStopMsg() throws IOException {
    String timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC).toString();
    String timestampData = String.format("{\"timestamp\":\"%s\"}", timestamp);


    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData(timestampData);

    new RetryTimeout().accept(pubsubMessage, null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat(String.format("Dropping event %s.", timestampData)).isEqualTo(logMessage);
  }

  @Test
  public void infiniteRetries_handlesEmptyMsg() throws IOException {
    PubSubMessage pubsubMessage = new PubSubMessage();
    pubsubMessage.setData("");

    new RetryTimeout().accept(new PubSubMessage(), null);

    String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Processing event null.").isEqualTo(logMessage);
  }
}
