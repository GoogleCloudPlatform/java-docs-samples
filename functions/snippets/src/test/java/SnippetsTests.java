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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsTests {

  private HttpServletRequest request;
  private HttpServletResponse response;

  private ByteArrayOutputStream stdOut;
  private StringWriter responseOut;

  @Rule
  public final EnvironmentVariables environmentVariables
    = new EnvironmentVariables();

  @Before
  public void beforeTest() throws Exception {
    // Use a new mock for each test
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);

    responseOut = new StringWriter();
    PrintWriter writer = new PrintWriter(responseOut);
    when(response.getWriter()).thenReturn(writer);

    // Capture std out
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterTest() {
    request = null;
    response = null;
    responseOut = null;
    stdOut = null;
    System.setOut(null);
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
}
