/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.requests;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link LoggingServlet}.
 */
@RunWith(JUnit4.class)
public class LoggingServletTest {
  // To capture and restore stderr
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
  private static final PrintStream REAL_ERR = System.err;

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private LoggingServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    //  Capture stderr to examine messages written to it
    System.setErr(new PrintStream(stderr));

    MockitoAnnotations.initMocks(this);

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new LoggingServlet();
  }

  @After
  public void tearDown() {
    //  Restore stderr
    System.setErr(LoggingServletTest.REAL_ERR);
  }

  @Test
  public void testListLogs() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    String out = stderr.toString();

    // We expect three log messages to be created
    // with the following messages.
    assertThat(out).contains("An informational message.");
    assertThat(out).contains("A warning message.");
    assertThat(out).contains("An error message.");
  }

}
