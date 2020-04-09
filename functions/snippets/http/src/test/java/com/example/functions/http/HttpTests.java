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

package com.example.functions.http;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class HttpTests {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

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
  public void corsEnabledAuthTest() throws IOException {
    when(request.getMethod()).thenReturn("GET");

    new CorsEnabledAuth().service(request, response);

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
}
