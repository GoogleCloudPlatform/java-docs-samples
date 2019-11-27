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

package com.example.guestbook;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class SignGuestbookServletTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;

  private SignGuestbookServlet signGuestbookServlet;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    // Sets up the UserServiceFactory used in SignGuestbookServlet (but not in this test)
    helper.setUp();

    signGuestbookServlet = new SignGuestbookServlet();
    TestUtils.startDatastore();
  }

  @Test
  public void doPost_userNotLoggedIn() throws Exception {
    String testBook = "default";
    when(mockRequest.getParameter("guestbookName")).thenReturn(testBook);
    String testGreeting = "beep!";
    when(mockRequest.getParameter("content")).thenReturn(testGreeting);

    signGuestbookServlet.doPost(mockRequest, mockResponse);
    Guestbook guestbook = new Guestbook(testBook);
    List<Greeting> greetings = guestbook.getGreetings();

    assertTrue(greetings.size() == 1);
    assertTrue(greetings.get(0).content.equals(testGreeting));
  }

  @After
  public void tearDown() {
    TestUtils.stopDatastore();
  }
}
