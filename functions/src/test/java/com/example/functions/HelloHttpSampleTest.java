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

// [START functions_http_unit_test]
package com.example.functions;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// [END functions_http_unit_test]

/**
 * Unit tests for {@link HelloHttpSample}.
 */
// [START functions_http_unit_test]
@RunWith(JUnit4.class)
public class HelloHttpSampleTest {

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;

  private HelloHttpSample sampleUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(mockRequest.getReader()).thenReturn(reader);

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    sampleUnderTest = new HelloHttpSample();
  }

  @Test
  public void getRequestNoParams() throws Exception {
    sampleUnderTest.helloWorld(mockRequest, mockResponse);
    assertThat(responseWriter.toString()).named("gcfSamples response").isEqualTo("Hello world!");
  }

  @Test
  public void getRequestUrlParams() throws Exception {
    when(mockRequest.getParameter("name")).thenReturn("Tom");
    sampleUnderTest.helloWorld(mockRequest, mockResponse);
    assertThat(responseWriter.toString()).named("gcfSamples response").isEqualTo("Hello Tom!");
  }

  @Test
  public void postRequestBodyParams() throws Exception {
    BufferedReader jsonReader = new BufferedReader(new StringReader("{'name': 'Jane'}"));
    when(mockRequest.getReader()).thenReturn(jsonReader);
    sampleUnderTest.helloWorld(mockRequest, mockResponse);
    assertThat(responseWriter.toString()).named("gcfSamples response").isEqualTo("Hello Jane!");
  }
}
// [END functions_http_unit_test]
