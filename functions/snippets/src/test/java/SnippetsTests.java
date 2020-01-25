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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;
import javax.servlet.ServletInputStream;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.google.common.truth.Truth;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "com.google.*"})
@PrepareForTest(StackdriverLogging.class)
public class SnippetsTests {
  @Mock private Logger loggerInstance;

  private HttpRequest request;
  private HttpResponse response;

  private ByteArrayOutputStream stdOut;
  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  private EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void beforeTest() throws Exception {
    // Use a new mock for each test
    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    // Capture std out
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Capture logs
    loggerInstance = mock(Logger.class);
    PowerMockito.mockStatic(Logger.class);

    Mockito.when(Logger.getLogger(anyString())).thenReturn(loggerInstance);
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
    when(request.getContentType()).thenReturn(Optional.of("application/json"));
    BufferedReader bodyReader = new BufferedReader(new StringReader("{\"name\":\"John\"}"));
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
    byte[] b64Body = Base64.getEncoder().encode("John".getBytes(Charset.defaultCharset()));
    ServletInputStream bodyInputStream = mock(ServletInputStream.class);
    when(bodyInputStream.readAllBytes()).thenReturn(b64Body);
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
    new Lazy().service(request, response);

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
    StackdriverLogging.PubSubMessage message = gson.fromJson(
        "{\"data\":\"data\",\"messageId\":\"id\"}", StackdriverLogging.PubSubMessage.class);
    new StackdriverLogging().accept(message, null);

    verify(loggerInstance, times(1)).info("Hello, data");
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
  public void helloHttp_noParamsGet() throws Exception {
    new HelloHttpSample().service(request, response);

    writerOut.flush();
    Truth.assertThat(responseOut.toString()).isEqualTo("Hello world!");
  }

  @Test
  public void helloHttp_urlParamsGet() throws Exception {
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("Tom"));

    new HelloHttpSample().service(request, response);

    writerOut.flush();
    Truth.assertThat(responseOut.toString()).isEqualTo("Hello Tom!");
  }

  @Test
  public void helloHttp_bodyParamsPost() throws Exception {
    BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));
    when(request.getReader()).thenReturn(jsonReader);

    new HelloHttpSample().service(request, response);
    writerOut.flush();

    Truth.assertThat(responseOut.toString()).isEqualTo("Hello Jane!");
  }
}
