/*
 * Copyright 2019 Google Inc.
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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.appengine.pubsub.Data;
import com.example.appengine.pubsub.PubSub;
import com.google.api.core.ApiFuture;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link PubSub}. */
@RunWith(JUnit4.class)
public class PubSubTest {
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  @Mock RequestDispatcher requestDispatcher;

  private PubSub pubSubServlet;

  @Mock ApiFuture<String> future;

  private ServletContext servletContext;

  private ServletConfig servletConfig;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    pubSubServlet = new PubSub();
    servletConfig = Mockito.mock(ServletConfig.class);
    servletContext = Mockito.mock(ServletContext.class);
    pubSubServlet.init(servletConfig);
    servletContext.setAttribute("data", new Data());
  }

  @Test
  public void testIndex() throws Exception {
    when(mockRequest.getRequestDispatcher("index.jsp")).thenReturn(requestDispatcher);
    when(pubSubServlet.getServletContext()).thenReturn(servletContext);
    pubSubServlet.doGet(mockRequest, mockResponse);
    verify(mockRequest, atLeast(1)).getRequestDispatcher("index.jsp");
  }

  @Test
  public void testPost() throws Exception {
    when(mockRequest.getParameter("payload")).thenReturn("test");
    when(future.get()).thenReturn("630244882789845");
    pubSubServlet.doPost(mockRequest, mockResponse);
    verify(mockRequest, atLeast(1)).getParameter("payload");
  }
}
