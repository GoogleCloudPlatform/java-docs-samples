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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.truth.Truth;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SnippetsTests {
  @Mock private Logger loggerInstance;

  private HttpServletRequest request;
  private HttpServletResponse response;

  private ByteArrayOutputStream stdOut;
  private StringWriter responseOut;

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public final EnvironmentVariables environmentVariables
      = new EnvironmentVariables();

  @Before
  public void beforeTest() throws Exception {
    // Use a new mock for each test
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    PrintWriter writer = new PrintWriter(responseOut);
    when(response.getWriter()).thenReturn(writer);

    // Capture std out
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Capture logs
    loggerInstance = mock(Logger.class);
    PowerMockito.mockStatic(Logger.class);

    when(Logger.getLogger(anyString())).thenReturn(loggerInstance);
  }

  @After
  public void afterTest() {
    request = null;
    response = null;
    responseOut = null;
    stdOut = null;
    System.setOut(null);
    Mockito.reset();
  }

  @Test
  public void helloWorldTest() throws IOException {
    new HelloWorld().helloGet(request, response);

    assertThat(responseOut.toString(), containsString("Hello World!"));
  }

  @Test
  public void logHelloWorldTest() throws IOException {
    new LogHelloWorld().logHelloWorld(request, response);

    assertThat(responseOut.toString(), containsString("Messages successfully logged!"));
  }

  @Test
  public void sendHttpRequestTest() throws IOException, InterruptedException {
    new SendHttpRequest().sendHttpRequest(request, response);

    assertThat(responseOut.toString(), containsString("Received code "));
  }

  @Test
  public void corsEnabledTest() throws IOException {
    when(request.getMethod()).thenReturn("GET");

    new CorsEnabled().corsEnabled(request, response);

    assertThat(responseOut.toString(), containsString("CORS headers set successfully!"));
  }

  @Test
  public void parseContentTypeTest_json() throws IOException {
    // Send a request with JSON data
    when(request.getContentType()).thenReturn("application/json");
    BufferedReader bodyReader = new BufferedReader(new StringReader("{\"name\":\"John\"}"));
    when(request.getReader()).thenReturn(bodyReader);

    new ParseContentType().parseContentType(request, response);

    assertThat(responseOut.toString(), containsString("Hello John!"));
  }

  @Test
  public void parseContentTypeTest_base64() throws IOException {
    // Send a request with octet-stream
    when(request.getContentType()).thenReturn("application/octet-stream");
    // Create mock input stream to return the data
    byte[] b64Body = Base64.getEncoder().encode("John".getBytes());
    ServletInputStream bodyInputStream = mock(ServletInputStream.class);
    when(bodyInputStream.readAllBytes()).thenReturn(b64Body);
    // Return the input stream when the request calls it
    when(request.getInputStream()).thenReturn(bodyInputStream);

    new ParseContentType().parseContentType(request, response);

    assertThat(responseOut.toString(), containsString("Hello John!"));
  }

  @Test
  public void parseContentTypeTest_text() throws IOException {
    // Send a request with plain text
    when(request.getContentType()).thenReturn("text/plain");
    BufferedReader bodyReader = new BufferedReader(new StringReader("John"));
    when(request.getReader()).thenReturn(bodyReader);

    new ParseContentType().parseContentType(request, response);

    assertThat(responseOut.toString(), containsString("Hello John!"));
  }

  @Test
  public void parseContentTypeTest_form() throws IOException {
    // Send a request with plain text
    when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
    when(request.getParameter("name")).thenReturn("John");

    new ParseContentType().parseContentType(request, response);

    assertThat(responseOut.toString(), containsString("Hello John!"));
  }

  @Test
  public void scopesTest() throws IOException {
    new Scopes().scopeDemo(request, response);

    assertThat(responseOut.toString(), containsString("Instance:"));
  }

  @Test
  public void lazyTest() throws IOException {
    new Lazy().lazyGlobal(request, response);

    assertThat(responseOut.toString(), containsString("Lazy global:"));
  }

  @Test
  public void retrieveLogsTest() throws IOException {
    new RetrieveLogs().retrieveLogs(request, response);

    assertThat(responseOut.toString(), containsString("Logs retrieved successfully."));
  }

  @Test
  public void helloBackgroundTest() throws IOException {
    when(request.getContentType()).thenReturn("application/json");
    BufferedReader bodyReader = new BufferedReader(new StringReader("{\"name\":\"John\"}"));
    when(request.getReader()).thenReturn(bodyReader);

    new HelloBackground().helloBackground(request, response);
    assertThat(responseOut.toString(), containsString("Hello John!"));
  }

  @Test
  public void filesTest() throws IOException {
    new FileSystem().listFiles(request, response);
    assertThat(responseOut.toString(), containsString("Files:"));
  }

  @Test
  public void logEntry() throws IOException {
    LogEntry.PubSubMessage message = gson.fromJson(
        "{\"data\":\"data\",\"messageId\":\"id\"}", LogEntry.PubSubMessage.class);
    String res = new LogEntry().helloPubSub(message);
    assertThat(res, containsString("Hello, data"));
  }

  @Test
  public void envTest() throws IOException {
    environmentVariables.set("FOO", "BAR");
    new EnvVars().envVar(request, response);
    assertThat(responseOut.toString(), containsString("BAR"));
  }

  @Test
  public void helloExecutionCount() throws IOException {
    new Concepts().executionCount(request, response);
    assertThat(responseOut.toString(), containsString("Instance execution count: 1"));
  }

  public void helloHttp_noParamsGet() throws Exception {
    new HelloHttpSample().helloWorld(request, response);
    Truth.assertThat(responseOut.toString()).isEqualTo("Hello world!");
  }

  @Test
  public void helloHttp_urlParamsGet() throws Exception {
    when(request.getParameter("name")).thenReturn("Tom");

    new HelloHttpSample().helloWorld(request, response);
    Truth.assertThat(responseOut.toString()).isEqualTo("Hello Tom!");
  }

  @Test
  public void helloHttp_bodyParamsPost() throws Exception {
    BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));
    when(request.getReader()).thenReturn(jsonReader);

    new HelloHttpSample().helloWorld(request, response);
    Truth.assertThat(responseOut.toString()).isEqualTo("Hello Jane!");
  }

  @PrepareForTest({Logger.class, HelloBackgroundSample.class})
  @Test
  public void helloBackground_printsName() throws Exception {
    BackgroundEvent event = new BackgroundEvent();
    event.name = "John";

    new HelloBackgroundSample().helloBackground(event);
    verify(loggerInstance, times(1)).info("Hello John!");
  }

  @PrepareForTest({Logger.class, HelloBackgroundSample.class})
  @Test
  public void helloBackground_printsHelloWorld() throws Exception {
    BackgroundEvent event = new BackgroundEvent();
    new HelloBackgroundSample().helloBackground(event);

    verify(loggerInstance, times(1)).info("Hello world!");
  }

  @PrepareForTest({Logger.class, HelloGcsSample.class})
  @Test
  public void helloGcs_shouldPrintUploadedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "foo.txt";
    event.metageneration = "1";
    new HelloGcsSample().helloGcs(event);
    verify(loggerInstance, times(1)).info("File foo.txt uploaded.");
  }

  @PrepareForTest({Logger.class, HelloGcsSample.class})
  @Test
  public void helloGcs_shouldPrintMetadataUpdatedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "baz.txt";
    event.metageneration = "2";
    new HelloGcsSample().helloGcs(event);
    verify(loggerInstance, times(1)).info("File baz.txt metadata updated.");
  }

  @PrepareForTest({Logger.class, HelloPubSubSample.class})
  @Test
  public void helloPubSub_shouldPrintName() throws Exception {
    PubSubMessage message = new PubSubMessage();
    message.data = Base64.getEncoder().encodeToString("John".getBytes());
    new HelloPubSubSample().helloPubSub(message);
    verify(loggerInstance, times(1)).info("Hello John!");
  }

  @PrepareForTest({Logger.class, HelloPubSubSample.class})
  @Test
  public void helloPubSub_shouldPrintHelloWorld() throws Exception {
    PubSubMessage message = new PubSubMessage();
    new HelloPubSubSample().helloPubSub(message);
    verify(loggerInstance, times(1)).info("Hello world!");
  }
}
