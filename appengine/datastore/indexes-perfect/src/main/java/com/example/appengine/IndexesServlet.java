/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet to demonstrate the use of Cloud Datastore indexes.
 */
public class IndexesServlet extends HttpServlet {
  private final DatastoreService datastore;

  public IndexesServlet() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    PrintWriter out = resp.getWriter();
    // These queries should all work with the same index.
    // [START queries_and_indexes_example_1]
    Query q1 =
        new Query("Person")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("lastName", FilterOperator.EQUAL, "Smith"),
                    new FilterPredicate("height", FilterOperator.EQUAL, 72)))
            .addSort("height", Query.SortDirection.DESCENDING);
    // [END queries_and_indexes_example_1]
    List<Entity> r1 = datastore.prepare(q1).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d results from query 1.\n", r1.size());

    // [START queries_and_indexes_example_2]
    Query q2 =
        new Query("Person")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("lastName", FilterOperator.EQUAL, "Jones"),
                    new FilterPredicate("height", FilterOperator.EQUAL, 63)))
            .addSort("height", Query.SortDirection.DESCENDING);
    // [END queries_and_indexes_example_2]
    List<Entity> r2 = datastore.prepare(q2).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d results from query 2.\n", r2.size());

    // [START queries_and_indexes_example_3]
    Query q3 =
        new Query("Person")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("lastName", FilterOperator.EQUAL, "Friedkin"),
                    new FilterPredicate("firstName", FilterOperator.EQUAL, "Damian")))
            .addSort("height", Query.SortDirection.ASCENDING);
    // [END queries_and_indexes_example_3]
    List<Entity> r3 = datastore.prepare(q3).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d results from query 3.\n", r3.size());

    // [START queries_and_indexes_example_4]
    Query q4 =
        new Query("Person")
            .setFilter(new FilterPredicate("lastName", FilterOperator.EQUAL, "Blair"))
            .addSort("firstName", Query.SortDirection.ASCENDING)
            .addSort("height", Query.SortDirection.ASCENDING);
    // [END queries_and_indexes_example_4]
    List<Entity> r4 = datastore.prepare(q4).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d results from query 4.\n", r4.size());
  }
}
