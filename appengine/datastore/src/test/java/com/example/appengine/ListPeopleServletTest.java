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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
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
 * Unit tests for {@link ListPeopleServlet}.
 */
@RunWith(JUnit4.class)
public class ListPeopleServletTest {
  private static final ImmutableList<String> TEST_NAMES =
      // Keep in alphabetical order, so this is the same as the query order.
      ImmutableList.<String>builder()
          .add("Alpha")
          .add("Bravo")
          .add("Charlie")
          .add("Delta")
          .add("Echo")
          .add("Foxtrot")
          .add("Golf")
          .add("Hotel")
          .add("India")
          .add("Juliett")
          .add("Kilo")
          .add("Lima")
          .add("Mike")
          .add("November")
          .add("Oscar")
          .add("Papa")
          .add("Quebec")
          .add("Romeo")
          .add("Sierra")
          .add("Tango")
          .build();

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private DatastoreService datastore;

  private ListPeopleServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();

    // Add test data.
    ImmutableList.Builder<Entity> people = ImmutableList.builder();
    for (String name : TEST_NAMES) {
      people.add(createPerson(name));
    }
    datastore.put(people.build());

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new ListPeopleServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private Entity createPerson(String name) {
    Entity person = new Entity("Person");
    person.setProperty("name", name);
    return person;
  }

  @Test
  public void doGet_noCursor_writesNames() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    String response = responseWriter.toString();
    for (int i = 0; i < ListPeopleServlet.PAGE_SIZE; i++) {
      assertThat(response).named("ListPeopleServlet response").contains(TEST_NAMES.get(i));
    }
  }

  private String getFirstCursor() {
    Query q = new Query("Person").addSort("name", SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(q);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(ListPeopleServlet.PAGE_SIZE);
    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
    return results.getCursor().toWebSafeString();
  }

  @Test
  public void doGet_withValidCursor_writesNames() throws Exception {
    when(mockRequest.getParameter("cursor")).thenReturn(getFirstCursor());

    servletUnderTest.doGet(mockRequest, mockResponse);

    String response = responseWriter.toString();
    int i = 0;
    while (i + ListPeopleServlet.PAGE_SIZE < TEST_NAMES.size() && i < ListPeopleServlet.PAGE_SIZE) {
      assertThat(response)
          .named("ListPeopleServlet response")
          .contains(TEST_NAMES.get(i + ListPeopleServlet.PAGE_SIZE));
      i++;
    }
  }

  @Test
  public void doGet_withInvalidCursor_writesRedirect() throws Exception {
    when(mockRequest.getParameter("cursor")).thenReturn("ThisCursorIsTotallyInvalid");
    servletUnderTest.doGet(mockRequest, mockResponse);
    verify(mockResponse).sendRedirect("/people");
  }
}
