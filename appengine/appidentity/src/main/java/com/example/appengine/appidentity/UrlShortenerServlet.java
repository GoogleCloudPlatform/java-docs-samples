/*
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

import java.io.IOException;
import java.io.PrintWriter;

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
    PrintWriter writer = resp.getWriter();
    writer.println("<!DOCTYPE html>");
    writer.println("<meta charset=\"utf-8\">");
    writer.println(
        "<title>Asserting Identity to Google APIs - App Engine App Identity Example</title>");
    writer.println("<form method=\"post\">");
    writer.println("<label for=\"longUrl\">URL:</label>");
    writer.println("<input id=\"longUrl\" name=\"longUrl\" type=\"text\">");
    writer.println("<input type=\"submit\" value=\"Shorten\">");
    writer.println("</form>");
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
    PrintWriter writer = resp.getWriter();
    try {
      shortUrl = shortener.createShortUrl(longUrl);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writer.println("error shortening URL: " + longUrl);
      e.printStackTrace(writer);
      return;
    }

    writer.print("long URL: ");
    writer.println(longUrl);
    writer.print("short URL: ");
    writer.println(shortUrl);
  }
}
