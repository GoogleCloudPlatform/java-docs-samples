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

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.appengine.api.NamespaceManager;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START datastore]
// [START tq_1]
public class UpdateCountsServlet extends HttpServlet {

  private static final int NUM_RETRIES = 10;

  @Entity
  public class CounterPojo {

    @Id
    public Long id;
    @Index
    public String name;
    public Long count;

    public CounterPojo() {
      this.count = 0L;
    }

    public CounterPojo(String name) {
      this.name = name;
      this.count = 0L;
    }

    public void increment() {
      count++;
    }
  }

  /**
   * Increment the count in a Counter datastore entity.
   **/
  public long updateCount(String countName) {

    CounterPojo cp = ofy().load().type(CounterPojo.class).filter("name", countName).first().now();
    if (cp == null) {
      cp = new CounterPojo(countName);
    }
    cp.increment();
    ofy().save().entity(cp).now();

    return cp.count;
  }
  // [END tq_1]

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws java.io.IOException {

    // Update the count for the current namespace.
    updateCount("request");

    // Update the count for the "-global-" namespace.
    String namespace = NamespaceManager.get();
    try {
      // "-global-" is namespace reserved by the application.
      NamespaceManager.set("-global-");
      updateCount("request");
    } finally {
      NamespaceManager.set(namespace);
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Counts are now updated.");
  }
  // [END datastore]

  // [START tq_2]
  // called from Task Queue
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String[] countName = req.getParameterValues("countName");
    if (countName.length != 1) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    updateCount(countName[0]);
  }
  // [END tq_2]
}
