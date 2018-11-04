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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@SuppressWarnings("serial")
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "URLFetch",
    description = "URLFetch: Write low order IP address to Cloud SQL",
    urlPatterns = "/urlfetch")
public class UrlFetchServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    // [START example]
    URL url = new URL("http://api.icndb.com/jokes/random");
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    StringBuffer json = new StringBuffer();
    String line;

    while ((line = reader.readLine()) != null) {
      json.append(line);
    }
    reader.close();
    // [END example]
    JSONObject jo = new JSONObject(json.toString());

    req.setAttribute("joke", jo.getJSONObject("value").getString("joke"));
    req.getRequestDispatcher("/urlfetchresult.jsp").forward(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    String id = req.getParameter("id");
    String text = req.getParameter("text");

    // Validation for id and text inputs.
    if (id == null
        || text == null
        || id.isEmpty()
        || text.isEmpty()
        || Integer.parseInt(id) > 100) {
      req.setAttribute("error", "invalid input");
      req.getRequestDispatcher("/urlfetchresult.jsp").forward(req, resp);
      return;
    }

    // [START complex]
    URL url = new URL("http://jsonplaceholder.typicode.com/posts/" + id);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Enable output for the connection.
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    conn.setRequestProperty("Accept", "application/json");
    // Set HTTP request method.
    conn.setRequestMethod("PUT");

    // Create JSON request.
    JSONObject jsonObj =
        new JSONObject().put("userId", 1).put("id", id).put("title", text).put("body", text);

    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
    writer.write(jsonObj.toString());
    writer.close();

    int respCode = conn.getResponseCode(); // New items get NOT_FOUND on PUT
    if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
      req.setAttribute("error", "");
      StringBuffer response = new StringBuffer();
      String line;

      // Read input data stream.
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      req.setAttribute("response", response.toString());
    } else {
      req.setAttribute("error", conn.getResponseCode() + " " + conn.getResponseMessage());
    }
    // [END complex]
    req.getRequestDispatcher("/urlfetchresult.jsp").forward(req, resp);
  }
}
