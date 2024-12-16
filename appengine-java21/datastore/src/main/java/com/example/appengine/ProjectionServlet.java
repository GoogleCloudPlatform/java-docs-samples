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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to demonstrate use of Datastore projection queries.
 *
 * <p>See the
 * <a href="https://cloud.google.com/appengine/docs/java/datastore/projectionqueries">documentation</a>
 * for using Datastore projection queries from the Google App Engine standard environment.
 */
@SuppressWarnings("serial")
public class ProjectionServlet extends HttpServlet {

  private static final String GUESTBOOK_ID = GuestbookStrongServlet.GUESTBOOK_ID;
  private final DatastoreService datastore;

  public ProjectionServlet() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.setCharacterEncoding("UTF-8");
    PrintWriter out = resp.getWriter();
    out.printf("Latest entries from guestbook: \n");

    Key guestbookKey = KeyFactory.createKey("Guestbook", GUESTBOOK_ID);
    Query query = new Query("Greeting", guestbookKey);
    addGuestbookProjections(query);
    printGuestbookEntries(datastore, query, out);
  }

  private void addGuestbookProjections(Query query) {
    query.addProjection(new PropertyProjection("content", String.class));
    query.addProjection(new PropertyProjection("date", Date.class));
  }

  private void printGuestbookEntries(DatastoreService datastore, Query query, PrintWriter out) {
    List<Entity> guests = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
    for (Entity guest : guests) {
      String content = (String) guest.getProperty("content");
      Date stamp = (Date) guest.getProperty("date");
      out.printf("Message %s posted on %s.\n", content, stamp.toString());
    }
  }
}
