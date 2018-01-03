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


package com.example.guestbook;

import static com.example.guestbook.GuestbookTestUtilities.cleanDatastore;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
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
 * Unit tests for {@link com.example.guestbook.SignGuestbookServlet}.
 */
@RunWith(JUnit4.class)
public class SignGuestbookServletTest {
  private static final String FAKE_URL = "fakey.org/sign";
  private static final String FAKE_NAME = "Fake";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private final String testPhrase = "Noew is the time";

  @Mock private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  private StringWriter stringWriter;
  private SignGuestbookServlet servletUnderTest;
  private Closeable closeable;
  private DatastoreService ds;

  @Before
  public void setUp() throws Exception {

    MockitoAnnotations.initMocks(this);
    helper.setUp();
    ds = DatastoreServiceFactory.getDatastoreService();

    //  Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);
    when(mockRequest.getParameter("guestbookName")).thenReturn("default");
    when(mockRequest.getParameter("content")).thenReturn(testPhrase);

    stringWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(stringWriter));

    servletUnderTest = new SignGuestbookServlet();

    ObjectifyService.register(Guestbook.class);
    ObjectifyService.register(Greeting.class);

    closeable = ObjectifyService.begin();

    cleanDatastore(ds, "default");
  }

  @After public void tearDown() {
    cleanDatastore(ds, "default");
    helper.tearDown();
    closeable.close();
  }

  @Test
  public void doPost_userNotLoggedIn() throws Exception {
    servletUnderTest.doPost(mockRequest, mockResponse);

    Query query = new Query("Greeting")
        .setAncestor(new KeyFactory.Builder("Guestbook", "default").getKey());
    PreparedQuery pq = ds.prepare(query);

    Entity greeting = pq.asSingleEntity();    // Should only be one at this point.
    assertEquals(greeting.getProperty("content"), testPhrase);
  }

}
