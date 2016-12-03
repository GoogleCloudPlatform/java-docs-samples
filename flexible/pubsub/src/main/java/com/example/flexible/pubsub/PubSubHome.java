/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.flexible.pubsub;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "main", value = "/")
public class PubSubHome extends HttpServlet {
  private static final String ENTRY_FIELD = "messages";

  private String entryKind = "pushed_message";

  public void setEntryKind(String kind) {
    entryKind = kind;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Prepare to present list
    resp.setContentType("text/html");
    PrintWriter out = resp.getWriter();
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<title>Send a PubSub Message</title>\n"
        + "</head>\n"
        + "<body>");

    // Get Messages
    List<String> messageList = getMessages();

    // Display received messages
    out.println("Received Messages:<br />");
    for (String message : messageList) {
      out.printf("%s<br />\n", message);
    }

    // Add Form to publish a new message
    out.println("<form action=\"publish\" method=\"POST\">\n"
        + "<label for=\"payload\">Message:</label>\n"
        + "<input id=\"payload\" type=\"input\"  name=\"payload\" />\n"
        + "<input id=\"submit\"  type=\"submit\" value=\"Send\" />\n"
        + "</form>");

    // Finish HTML Response
    out.println("</body>\n"
        + "</html>");
  }

  private List<String> getMessages() {
    // Get Message saved in Datastore
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query = Query.newEntityQueryBuilder()
        .setKind(entryKind)
        .setLimit(1)
        .build();
    QueryResults<Entity> results = datastore.run(query);

    // Convert stored JSON into List<String>
    List<String> messageList = new LinkedList<>();
    Gson gson = new Gson();
    if (results.hasNext()) {
      Entity entity = results.next();
      messageList = gson.fromJson(entity.getString(ENTRY_FIELD),
          messageList.getClass());
    }

    return messageList;
  }
}
