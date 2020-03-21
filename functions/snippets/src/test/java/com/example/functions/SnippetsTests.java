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

package com.example.functions;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;

public class SnippetsTests {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger BACKGROUND_LOGGER = Logger.getLogger(HelloBackground.class.getName());
  private static final Logger PUBSUB_LOGGER = Logger.getLogger(HelloPubSub.class.getName());
  private static final Logger GCS_LOGGER = Logger.getLogger(HelloGcs.class.getName());
  private static final Logger STACKDRIVER_LOGGER = Logger.getLogger(
      StackdriverLogging.class.getName());

  private static final TestLogHandler logHandler = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void beforeTest() throws IOException {
    BACKGROUND_LOGGER.addHandler(logHandler);
    PUBSUB_LOGGER.addHandler(logHandler);
    GCS_LOGGER.addHandler(logHandler);
    STACKDRIVER_LOGGER.addHandler(logHandler);

    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    // Use the same logging handler for all tests
    Logger.getLogger(HelloBackground.class.getName()).addHandler(logHandler);
    Logger.getLogger(HelloPubSub.class.getName()).addHandler(logHandler);
    Logger.getLogger(HelloGcs.class.getName()).addHandler(logHandler);
  }

  @After
  public void afterTest() {
    System.out.flush();
    logHandler.flush();
  }

  @Test
  public void helloWorldTest() throws IOException {
    new HelloWorld().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello World!");
  }

  @Test
  public void logHelloWorldTest() throws IOException {
    new LogHelloWorld().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Messages successfully logged!");
  }

  @Test
  public void sendHttpRequestTest() throws IOException, InterruptedException {
    new SendHttpRequest().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Received code ");
  }

  @Test
  public void corsEnabledTest() throws IOException {
    when(request.getMethod()).thenReturn("GET");

    new CorsEnabled().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("CORS headers set successfully!");
  }

  @Test
  public void parseContentTypeTest_json() throws IOException {
    // Send a request with JSON data
    BufferedReader bodyReader = new BufferedReader(new StringReader("{\"name\":\"John\"}"));

    when(request.getContentType()).thenReturn(Optional.of("application/json"));
    when(request.getReader()).thenReturn(bodyReader);

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_base64() throws IOException {
    // Send a request with octet-stream
    when(request.getContentType()).thenReturn(Optional.of("application/octet-stream"));

    // Create mock input stream to return the data
    byte[] b64Body = Base64.getEncoder().encode("John".getBytes(StandardCharsets.UTF_8));
    InputStream bodyInputStream = new ByteArrayInputStream(b64Body);

    // Return the input stream when the request calls it
    when(request.getInputStream()).thenReturn(bodyInputStream);

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_text() throws IOException {
    // Send a request with plain text
    when(request.getContentType()).thenReturn(Optional.of("text/plain"));
    BufferedReader bodyReader = new BufferedReader(new StringReader("John"));

    when(request.getReader()).thenReturn(bodyReader);

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_form() throws IOException {
    // Send a request with plain text
    when(request.getContentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("John"));

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
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
  public void retrieveLogsTest() throws IOException {
    new RetrieveLogs().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Logs retrieved successfully.");
  }

  @Test
  public void filesTest() throws IOException {
    new FileSystem().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Files:");
  }

  @Test
  public void stackdriverLogging() throws IOException {
    PubSubMessage pubsubMessage = gson.fromJson(
        "{\"data\":\"ZGF0YQ==\",\"messageId\":\"id\"}", PubSubMessage.class);
    new StackdriverLogging().accept(pubsubMessage, null);

    String logMessage = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello, data").isEqualTo(logMessage);
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

  @Test
  public void firebaseAuth() throws IOException {
    new FirebaseAuth().accept("{\"foo\": \"bar\"}", null);
  }
}
