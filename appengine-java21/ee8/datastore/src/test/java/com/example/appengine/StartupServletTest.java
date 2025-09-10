/*
 * Copyright 2016 Google LLC
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

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
 * Unit tests for {@link StartupServlet}.
 */
@RunWith(JUnit4.class)
public class StartupServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google
          // .com/appengine/docs/java/tools/localunittesting
          // #Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private DatastoreService datastore;

  private StartupServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new StartupServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_emptyDatastore_writesOkay() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);
    assertWithMessage("StartupServlet response").that(responseWriter.toString()).isEqualTo("ok\n");
  }

  @Test
  public void doGet_emptyDatastore_writesPresidents() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    Filter nameFilter = new FilterPredicate("name", FilterOperator.EQUAL, "George Washington");
    Query q = new Query("Person").setFilter(nameFilter);
    Entity result = datastore.prepare(q).asSingleEntity();
    assertWithMessage("name").that(result.getProperty("name")).isEqualTo("George Washington");
  }

  @Test
  public void doGet_alreadyPopulated_writesOkay() throws Exception {
    datastore.put(
        new Entity(StartupServlet.IS_POPULATED_ENTITY, StartupServlet.IS_POPULATED_KEY_NAME));
    servletUnderTest.doGet(mockRequest, mockResponse);
    assertWithMessage("StartupServlet response").that(responseWriter.toString()).isEqualTo("ok\n");
  }
}
