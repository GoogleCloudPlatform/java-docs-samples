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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
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
 * Unit tests for {@link IndexesServlet}.
 */
@RunWith(JUnit4.class)
public class IndexesServletTest {

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
  private IndexesServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new IndexesServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_emptyDatastore_writesNoWidgets() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    assertThat(responseWriter.toString())
        .named("IndexesServlet response")
        .isEqualTo("Got 0 widgets.\n");
  }

  @SuppressWarnings("VariableDeclarationUsageDistance")
  @Test
  public void doGet_repeatedPropertyEntities_writesWidgets() throws Exception {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // [START exploding_index_example_3]
    Entity widget = new Entity("Widget");
    widget.setProperty("x", Arrays.asList(1, 2, 3, 4));
    widget.setProperty("y", Arrays.asList("red", "green", "blue"));
    widget.setProperty("date", new Date());
    datastore.put(widget);
    // [END exploding_index_example_3]

    servletUnderTest.doGet(mockRequest, mockResponse);

    assertThat(responseWriter.toString())
        .named("IndexesServlet response")
        .isEqualTo("Got 1 widgets.\n");
  }
}
