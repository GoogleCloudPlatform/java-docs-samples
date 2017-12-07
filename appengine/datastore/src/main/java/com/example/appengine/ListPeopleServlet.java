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

// [START cursors]

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ListPeopleServlet extends HttpServlet {
  static final int PAGE_SIZE = 15;
  private final DatastoreService datastore;

  public ListPeopleServlet() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(PAGE_SIZE);

    // If this servlet is passed a cursor parameter, let's use it.
    String startCursor = req.getParameter("cursor");
    if (startCursor != null) {
      fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
    }

    Query q = new Query("Person").addSort("name", SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(q);

    QueryResultList<Entity> results;
    try {
      results = pq.asQueryResultList(fetchOptions);
    } catch (IllegalArgumentException e) {
      // IllegalArgumentException happens when an invalid cursor is used.
      // A user could have manually entered a bad cursor in the URL or there
      // may have been an internal implementation detail change in App Engine.
      // Redirect to the page without the cursor parameter to show something
      // rather than an error.
      resp.sendRedirect("/people");
      return;
    }

    resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
    PrintWriter w = resp.getWriter();
    w.println("<!DOCTYPE html>");
    w.println("<meta charset=\"utf-8\">");
    w.println("<title>Cloud Datastore Cursor Sample</title>");
    w.println("<ul>");
    for (Entity entity : results) {
      w.println("<li>" + entity.getProperty("name") + "</li>");
    }
    w.println("</ul>");

    String cursorString = results.getCursor().toWebSafeString();

    // This servlet lives at '/people'.
    w.println("<a href='/people?cursor=" + cursorString + "'>Next page</a>");
  }
}
// [END cursors]
