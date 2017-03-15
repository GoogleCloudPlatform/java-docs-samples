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

package com.google.appengine.demos.asyncrest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for REST servlets.
 */
public class AbstractRestServlet extends HttpServlet {

  protected static final int MAX_RESULTS = 5;

  protected static final String STYLE = "<style type='text/css'>"
          + "  img.thumb:hover {height:50px}"
          + "  img.thumb {vertical-align:text-top}"
          + "  span.red {color: #ff0000}"
          + "  span.green {color: #00ff00}"
          + "  iframe {border: 0px}" + "</style>";

  protected static final String APPKEY = "com.google.appengine.demos.asyncrest.appKey";
  protected static final String APPKEY_ENV = "PLACES_APPKEY";
  protected static final String LOC_PARAM = "loc";
  protected static final String ITEMS_PARAM = "items";
  protected static final String LATITUDE_PARAM = "lat";
  protected static final String LONGITUDE_PARAM = "long";
  protected static final String RADIUS_PARAM = "radius";
  protected String key;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    // First try the servlet context init-param.
    String source = "InitParameter";
    key = servletConfig.getInitParameter(APPKEY);
    if (key == null || key.startsWith("${")) {
      source = "System Property";
      key = System.getProperty(APPKEY);
    }
    if (key == null || key.startsWith("${")) {
      source = "Environment Variable";
      key = System.getenv(APPKEY_ENV);
    }
    if (key == null) {
      throw new UnavailableException("Places App Key not set");
    }
    if (key.startsWith("${")) {
      throw new UnavailableException("Places App Key not expanded from " + source);
    }
  }

  public static String sanitize(String str) {
    if (str == null) {
      return null;
    }
    return str.replace("<", "?").replace("&", "?").replace("\n", "?");
  }

  protected String restQuery(String coordinates, String radius, String item) {
    try {
      return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + key
          + "&location=" + URLEncoder.encode(coordinates, "UTF-8")
          + "&types=" + URLEncoder.encode(item, "UTF-8")
          + "&radius=" + URLEncoder.encode(radius, "UTF-8");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String generateResults(Queue<Map<String, Object>> results) {
    StringBuilder thumbs = new StringBuilder();
    int resultCount = 0;
    Iterator<Map<String, Object>> itor = results.iterator();

    while (resultCount < MAX_RESULTS && itor.hasNext()) {
      Map map = (Map) itor.next();
      String name = (String) map.get("name");
      Object[] photos = (Object[]) map.get("photos");
      if (photos != null && photos.length > 0) {
        resultCount++;
        thumbs.append(
            "<img class='thumb' border='1px' height='40px' "
                + "src='" + getPhotoUrl((String) (((Map) photos[0]).get("photo_reference"))) + "' "
                + "title='" + name + "' />");
        thumbs.append("</a>&nbsp;");
      }
    }
    return thumbs.toString();
  }

  public String getPhotoUrl(String photoref) {
    return "https://maps.googleapis.com/maps/api/place/photo?key=" + key + "&photoreference=" + photoref
            + "&maxheight=40";
  }

  protected String ms(long nano) {
    BigDecimal dec = new BigDecimal(nano);
    return dec.divide(new BigDecimal(1000000L)).setScale(1, RoundingMode.UP).toString();
  }

  protected int width(long nano) {
    int width = (int) ((nano + 999999L) / 5000000L);
    if (width == 0) {
      width = 2;
    }
    return width;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    doGet(request, response);
  }

}
