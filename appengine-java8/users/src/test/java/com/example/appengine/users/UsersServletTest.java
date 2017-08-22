/*
 * Copyright 2015 Google Inc.
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

package com.example.appengine.users;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.management.remote.JMXPrincipal;
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
 * Unit tests for {@link UsersServlet}.
 */
@RunWith(JUnit4.class)
public class UsersServletTest {

  private static final String FAKE_URL = "fakey.fake.fak";
  private static final String FAKE_NAME = "Fake";
  // Set up a helper so that the ApiProxy returns a valid environment for local testing.
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Mock
  private HttpServletRequest mockRequestNotLoggedIn;
  @Mock
  private HttpServletRequest mockRequestLoggedIn;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private UsersServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    //  Set up some fake HTTP requests
    //  If the user isn't logged in, use this request
    when(mockRequestNotLoggedIn.getRequestURI()).thenReturn(FAKE_URL);
    when(mockRequestNotLoggedIn.getUserPrincipal()).thenReturn(null);

    //  If the user is logged in, use this request
    when(mockRequestLoggedIn.getRequestURI()).thenReturn(FAKE_URL);
    //  Most of the classes that implement Principal have been
    //  deprecated.  JMXPrincipal seems like a safe choice.
    when(mockRequestLoggedIn.getUserPrincipal()).thenReturn(new JMXPrincipal(FAKE_NAME));

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new UsersServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_userNotLoggedIn_writesResponse() throws Exception {
    servletUnderTest.doGet(mockRequestNotLoggedIn, mockResponse);

    // If a user isn't logged in, we expect a prompt
    //  to login to be returned.
    assertThat(responseWriter.toString())
        .named("UsersServlet response")
        .contains("<p>Please <a href=");
    assertThat(responseWriter.toString())
        .named("UsersServlet response")
        .contains("sign in</a>.</p>");
  }

  @Test
  public void doGet_userLoggedIn_writesResponse() throws Exception {
    servletUnderTest.doGet(mockRequestLoggedIn, mockResponse);

    // If a user is logged in, we expect a prompt
    // to logout to be returned.
    assertThat(responseWriter.toString())
        .named("UsersServlet response")
        .contains("<p>Hello, " + FAKE_NAME + "!");
    assertThat(responseWriter.toString()).named("UsersServlet response").contains("sign out");
  }
}
