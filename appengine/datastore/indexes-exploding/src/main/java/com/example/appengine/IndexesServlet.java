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
    Query q =
        new Query("Widget")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("x", FilterOperator.EQUAL, 1),
                    new FilterPredicate("y", FilterOperator.EQUAL, "red")))
            .addSort("date", Query.SortDirection.ASCENDING);
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());

    PrintWriter out = resp.getWriter();
    out.printf("Got %d widgets.\n", results.size());
  }
}
