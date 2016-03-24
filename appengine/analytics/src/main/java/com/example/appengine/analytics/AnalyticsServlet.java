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

package com.example.appengine.analytics;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class AnalyticsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    String trackingId = System.getenv("GA_TRACKING_ID");
    URIBuilder builder = new URIBuilder();
    builder.setScheme("http").setHost("www.google-analytics.com").setPath("/collect")
        .addParameter("v", "1") // API Version.
        .addParameter("tid", trackingId) // Tracking ID / Property ID.
        // Anonymous Client Identifier. Ideally, this should be a UUID that
        // is associated with particular user, device, or browser instance.
        .addParameter("cid", "555")
        .addParameter("t", "event") // Event hit type.
        .addParameter("ec", "example") // Event category.
        .addParameter("ea", "test action"); // Event action.
    URI uri = null;
    try {
      uri = builder.build();
    } catch (URISyntaxException e) {
      throw new ServletException("Problem building URI", e);
    }
    URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
    URL url = uri.toURL();
    fetcher.fetch(url);
    resp.getWriter().println("Event tracked.");
  }
}
// [END example]
