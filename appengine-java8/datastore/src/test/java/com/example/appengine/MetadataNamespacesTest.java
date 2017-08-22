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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests to demonstrate App Engine Datastore namespaces metadata.
 */
@RunWith(JUnit4.class)
public class MetadataNamespacesTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google
          // .com/appengine/docs/java/tools/localunittesting
          // #Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private StringWriter responseWriter;
  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    responseWriter = new StringWriter();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // [START queries_intro_example]
  void printAllNamespaces(DatastoreService ds, PrintWriter writer) {
    Query q = new Query(Entities.NAMESPACE_METADATA_KIND);

    for (Entity e : ds.prepare(q).asIterable()) {
      // A nonzero numeric id denotes the default namespace;
      // see <a href="#Namespace_Queries">Namespace Queries</a>, below
      if (e.getKey().getId() != 0) {
        writer.println("<default>");
      } else {
        writer.println(e.getKey().getName());
      }
    }
  }
  // [END queries_intro_example]

  @Test
  public void printAllNamespaces_printsNamespaces() throws Exception {
    datastore.put(new Entity("Simple"));
    NamespaceManager.set("another-namespace");
    datastore.put(new Entity("Simple"));

    printAllNamespaces(datastore, new PrintWriter(responseWriter));

    String response = responseWriter.toString();
    assertThat(response).contains("<default>");
    assertThat(response).contains("another-namespace");
  }

  // [START namespace_query_example]
  List<String> getNamespaces(DatastoreService ds, String start, String end) {

    // Start with unrestricted namespace query
    Query q = new Query(Entities.NAMESPACE_METADATA_KIND);
    List<Filter> subFilters = new ArrayList();
    // Limit to specified range, if any
    if (start != null) {
      subFilters.add(
          new FilterPredicate(
              Entity.KEY_RESERVED_PROPERTY,
              FilterOperator.GREATER_THAN_OR_EQUAL,
              Entities.createNamespaceKey(start)));
    }
    if (end != null) {
      subFilters.add(
          new FilterPredicate(
              Entity.KEY_RESERVED_PROPERTY,
              FilterOperator.LESS_THAN_OR_EQUAL,
              Entities.createNamespaceKey(end)));
    }

    q.setFilter(CompositeFilterOperator.and(subFilters));

    // Initialize result list
    List<String> results = new ArrayList<String>();

    // Build list of query results
    for (Entity e : ds.prepare(q).asIterable()) {
      results.add(Entities.getNamespaceFromNamespaceKey(e.getKey()));
    }

    // Return result list
    return results;
  }
  // [END namespace_query_example]

  @Test
  public void getNamespaces_returnsNamespaces() throws Exception {
    NamespaceManager.set("alpha");
    datastore.put(new Entity("Simple"));
    NamespaceManager.set("bravo");
    datastore.put(new Entity("Simple"));
    NamespaceManager.set("charlie");
    datastore.put(new Entity("Simple"));
    NamespaceManager.set("zed");
    datastore.put(new Entity("Simple"));

    List<String> results = getNamespaces(datastore, "bravo", "echo");

    assertThat(results).containsExactly("bravo", "charlie");
  }
}
