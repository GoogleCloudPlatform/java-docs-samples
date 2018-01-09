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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START tq_3]
public class SomeRequestServlet extends HttpServlet {

  // Handler for URL get requests.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // Increment the count for the current namespace asynchronously.
    QueueFactory.getDefaultQueue()
        .add(TaskOptions.Builder.withUrl("/_ah/update_count").param("countName", "SomeRequest"));
    // Increment the global count and set the
    // namespace locally.  The namespace is
    // transferred to the invoked request and
    // executed asynchronously.
    String namespace = NamespaceManager.get();
    try {
      NamespaceManager.set("-global-");
      QueueFactory.getDefaultQueue()
          .add(TaskOptions.Builder.withUrl("/_ah/update_count").param("countName", "SomeRequest"));
    } finally {
      NamespaceManager.set(namespace);
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Counts are being updated.");
  }
}
// [END tq_3]
