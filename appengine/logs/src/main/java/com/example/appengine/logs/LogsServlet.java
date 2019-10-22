/* Copyright 2016 Google Inc.
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

package com.example.appengine.logs;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;


// Get request logs along with their app log lines and display them 5 at
// a time, using a Next link to cycle through to the next 5.
public class LogsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws IOException {

    resp.setContentType("text/html");
    PrintWriter writer = resp.getWriter();
    writer.println("<!DOCTYPE html>");
    writer.println("<meta charset=\"utf-8\">");
    writer.println("<title>App Engine Logs Sample</title>");

    // We use this to break out of our iteration loop, limiting record
    // display to 5 request logs at a time.
    int limit = 5;

    // This retrieves the offset from the Next link upon user click.
    String offset = req.getParameter("offset");

    // We want the App logs for each request log
    LogQuery query = LogQuery.Builder.withDefaults();
    query.includeAppLogs(true);

    // Set the offset value retrieved from the Next link click.
    if (offset != null) {
      query.offset(offset);
    }

    // This gets filled from the last request log in the iteration
    String lastOffset = null;
    int count = 0;

    // Display a few properties of each request log.
    for (RequestLogs record : LogServiceFactory.getLogService().fetch(query)) {
      writer.println("<br>REQUEST LOG <br>");
      DateTime reqTime = new DateTime(record.getStartTimeUsec() / 1000);
      writer.println("IP: " + record.getIp() + "<br>");
      writer.println("Method: " + record.getMethod() + "<br>");
      writer.println("Resource " + record.getResource() + "<br>");
      writer.println(String.format("<br>Date: %s", reqTime.toString()));

      lastOffset = record.getOffset();

      // Display all the app logs for each request log.
      for (AppLogLine appLog : record.getAppLogLines()) {
        writer.println("<br>" + "APPLICATION LOG" + "<br>");
        DateTime appTime = new DateTime(appLog.getTimeUsec() / 1000);
        writer.println(String.format("<br>Date: %s", appTime.toString()));
        writer.println("<br>Level: " + appLog.getLogLevel() + "<br>");
        writer.println("Message: " + appLog.getLogMessage() + "<br> <br>");
      }

      if (++count >= limit) {
        break;
      }
    }

    // When the user clicks this link, the offset is processed in the
    // GET handler and used to cycle through to the next 5 request logs.
    writer.println(String.format("<br><a href=\"/?offset=%s\">Next</a>", lastOffset));
  }
}
