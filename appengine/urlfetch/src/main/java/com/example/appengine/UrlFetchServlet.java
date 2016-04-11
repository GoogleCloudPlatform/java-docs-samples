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

package com.example.appengine;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UrlFetchServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    PrintWriter out = resp.getWriter();
    out.println("<html><body>");

// [START example]
    URL url = new URL("http://api.icndb.com/jokes/random");
    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    String json = "";
    String line;

    while ((line = reader.readLine()) != null) {
      json += line;
    }
    reader.close();
// [END example]
    JSONObject jo = new JSONObject(json);
    out.println("<h2>"
        + jo.getJSONObject("value").getString("joke")
        + "</h2>");
    out.println("</body></html>");
  }
}
