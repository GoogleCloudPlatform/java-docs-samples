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

package com.example.appengine;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.example.time.testing.FakeClock;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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
 * Unit tests for {@link ProjectionServlet}.
 */
@RunWith(JUnit4.class)
public class ProjectionServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private ProjectionServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new ProjectionServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_emptyDatastore_writesNoGreetings() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    assertThat(responseWriter.toString())
        .named("ProjectionServlet response")
        .doesNotContain("Message");
  }

  @Test
  public void doGet_manyGreetings_writesLatestGreetings() throws Exception {
    // Arrange
    GuestbookStrong guestbook =
        new GuestbookStrong(GuestbookStrongServlet.GUESTBOOK_ID, new FakeClock());
    guestbook.appendGreeting("Hello.");
    guestbook.appendGreeting("Güten Tag!");
    guestbook.appendGreeting("Hi.");
    guestbook.appendGreeting("Hola.");

    // Act
    servletUnderTest.doGet(mockRequest, mockResponse);
    String output = responseWriter.toString();

    assertThat(output).named("ProjectionServlet response").contains("Message Hello.");
    assertThat(output).named("ProjectionServlet response").contains("Message Güten Tag!");
    assertThat(output).named("ProjectionServlet response").contains("Message Hola.");
  }
}
