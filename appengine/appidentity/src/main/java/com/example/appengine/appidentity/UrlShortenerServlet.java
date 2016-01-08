/**
 * Copyright 2016 Google Inc. All Rights Reserved.
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
package com.example.appengine.appidentity;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UrlShortenerServlet extends HttpServlet {
  private final UrlShortener shortener;

  public UrlShortenerServlet() {
    shortener = new UrlShortener();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter w = resp.getWriter();
    w.println("<!DOCTYPE html>");
    w.println("<meta charset=\"utf-8\">");
    w.println("<title>Asserting Identity to Google APIs - App Engine App Identity Example</title>");
    w.println("<form method=\"post\">");
    w.println("<label for=\"longUrl\">URL:</label>");
    w.println("<input id=\"longUrl\" name=\"longUrl\" type=\"text\">");
    w.println("<input type=\"submit\" value=\"Shorten\">");
    w.println("</form>");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    String longUrl = req.getParameter("longUrl");
    if (longUrl == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing longUrl parameter");
      return;
    }

    String shortUrl;
    PrintWriter w = resp.getWriter();
    try {
      shortUrl = shortener.createShortUrl(longUrl);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      w.println("error shortening URL: " + longUrl);
      e.printStackTrace(w);
      return;
    }

    w.print("long URL: ");
    w.println(longUrl);
    w.print("short URL: ");
    w.println(shortUrl);
  }
}
