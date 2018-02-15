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

package com.google.appengine.demos.asyncrest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.ajax.JSON;

/**
 * Servlet which makes REST calls serially.
 *
 * <p>May be configured with init parameters:
 * <dl>
 * <dt>appid</dt>
 * <dd>The Google app key to use</dd>
 * </dl>
 */

public class SerialRestServlet extends AbstractRestServlet {

  //CHECKSTYLE OFF: VariableDeclarationUsageDistance
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (key == null) {
      response.sendError(500, APPKEY + " not set");
      return;
    }

    long start = System.nanoTime();


    String loc = sanitize(request.getParameter(LOC_PARAM));
    String lat = sanitize(request.getParameter(LATITUDE_PARAM));
    String longitude = sanitize(request.getParameter(LONGITUDE_PARAM));
    String radius = sanitize(request.getParameter(RADIUS_PARAM));

    String[] keywords = sanitize(request.getParameter(ITEMS_PARAM)).split(",");
    Queue<Map<String, Object>> results = new LinkedList<Map<String, Object>>();

    // Make all requests serially.
    for (String itemName : keywords) {
      URL url = new URL(restQuery(lat + "," + longitude, radius, itemName));

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));

      Map query =
          (Map) JSON.parse(new BufferedReader(new InputStreamReader(connection.getInputStream())));
      Object[] tmp = (Object[]) query.get("results");
      if (tmp != null) {
        for (Object o : tmp) {
          Map map = (Map) o;
          results.add(map);
        }
      }
    }

    // Generate the response.
    String thumbs = generateResults(results);

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html><head>");
    out.println(STYLE);
    out.println("</head><body><small>");

    long now = System.nanoTime();
    long total = now - start;

    out.print(
        "<b>Blocking: Requesting "
            + sanitize(request.getParameter(ITEMS_PARAM))
            + " near "
            + (loc != null ? loc : "lat=" + lat + " long=" + longitude)
            + "</b><br/>");
    out.print("Total Time: " + ms(total) + "ms<br/>");
    out.print("Thread held (<span class='red'>red</span>): " + ms(total) + "ms<br/>");

    out.println(
        "<img border='0px' src='asyncrest/red.png' "
            + "height='20px' "
            + "width='" + width(total) + "px'>");

    out.println("<br/>");
    out.print("First 5 results of " + results.size() + ":<br/>");
    if ("".equals(thumbs)) {
      out.print("<i>No results. Ensure " + APPKEY + " property is set correctly.</i>");
    } else {
      out.println(thumbs);
    }
    out.println("</small>");
    out.println("</body></html>");
    out.close();
  }
  //CHECKSTYLE ON: VariableDeclarationUsageDistance

  /**
   * Handle HTTP POST request.
   *
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}