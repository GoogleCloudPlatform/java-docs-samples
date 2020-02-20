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

import static com.google.common.truth.Truth.assertThat;

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
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class SnippetsTests {
  private MockHttpRequest request;
  private MockHttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger backgroundLogger = Logger.getLogger(HelloBackground.class.getName());
  private static final Logger pubsubLogger = Logger.getLogger(HelloPubSub.class.getName());
  private static final Logger gcsLogger = Logger.getLogger(HelloGcs.class.getName());
  private static final Logger stackdriverLogger = Logger.getLogger(
      StackdriverLogging.class.getName());

  private static final TestLogHandler logHandler = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void beforeTest() throws IOException {
    backgroundLogger.addHandler(logHandler);
    pubsubLogger.addHandler(logHandler);
    gcsLogger.addHandler(logHandler);
    stackdriverLogger.addHandler(logHandler);

    // Use new mock objects for each test
    request = new MockHttpRequest();
    response = new MockHttpResponse();

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    request.bufferedReader = reader;

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    response.writer = writerOut;

    // Use the same logging handler for all tests
    Logger.getLogger(HelloBackground.class.getName()).addHandler(logHandler);
    Logger.getLogger(HelloPubSub.class.getName()).addHandler(logHandler);
    Logger.getLogger(HelloGcs.class.getName()).addHandler(logHandler);
  }

  @After
  public void afterTest() {
    request = null;
    response = null;
    responseOut = null;
    System.setOut(null);
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
    request.httpMethod = "GET";

    new CorsEnabled().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("CORS headers set successfully!");
  }

  @Test
  public void parseContentTypeTest_json() throws IOException {
    // Send a request with JSON data
    request.contentType = Optional.of("application/json");

    BufferedReader bodyReader = new BufferedReader(new StringReader("{\"name\":\"John\"}"));

    request.bufferedReader = bodyReader;

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_base64() throws IOException {
    // Send a request with octet-stream
    request.contentType = Optional.of("application/octet-stream");

    // Create mock input stream to return the data
    byte[] b64Body = Base64.getEncoder().encode("John".getBytes(StandardCharsets.UTF_8));
    InputStream bodyInputStream = new ByteArrayInputStream(b64Body);

    // Return the input stream when the request calls it
    request.inputStream = bodyInputStream;

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_text() throws IOException {
    // Send a request with plain text
    request.contentType = Optional.of("text/plain");
    BufferedReader bodyReader = new BufferedReader(new StringReader("John"));

    request.bufferedReader = bodyReader;

    new ParseContentType().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Hello John!");
  }

  @Test
  public void parseContentTypeTest_form() throws IOException {
    // Send a request with plain text
    request.contentType = Optional.of("application/x-www-form-urlencoded");
    request.queryParams.put("name", Arrays.asList(new String[]{"John"}));

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
    request.queryParams.put("name", Arrays.asList(new String[]{"Tom"}));

    new HelloHttp().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).isEqualTo("Hello Tom!");
  }

  @Test
  public void helloHttp_bodyParamsPost() throws IOException {
    BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));

    request.bufferedReader = jsonReader;

    new HelloHttp().service(request, response);
    writerOut.flush();

    assertThat(responseOut.toString()).isEqualTo("Hello Jane!");
  }

  @Test
  public void firebaseAuth() throws IOException {
    new FirebaseAuth().accept("{\"foo\": \"bar\"}", null);
  }
}
