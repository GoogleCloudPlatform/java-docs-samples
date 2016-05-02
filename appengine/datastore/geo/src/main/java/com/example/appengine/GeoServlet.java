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

package com.example.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.GeoRegion.Circle;
import com.google.appengine.api.datastore.Query.GeoRegion.Rectangle;
import com.google.appengine.api.datastore.Query.StContainsFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet to demonstrate the use of Cloud Datastore geospatial queries.
 */
public class GeoServlet extends HttpServlet {
  static final String BRAND_PROPERTY = "brand";
  static final String LOCATION_PROPERTY = "location";

  static final String BRAND_PARAMETER = "brand";
  static final String LATITUDE_PARAMETER = "lat";
  static final String LONGITUDE_PARAMETER = "lon";
  static final String RADIUS_PARAMETER = "r";

  static final String BRAND_DEFAULT = "Ocean Ave Shell";
  static final String LATITUDE_DEFAULT = "37.7895873";
  static final String LONGITUDE_DEFAULT = "-122.3917317";
  static final String RADIUS_DEFAULT = "1000.0";

  // Number of meters (approximately) in 1 degree of latitude.
  // http://gis.stackexchange.com/a/2964
  private static final double DEGREE_METERS = 111111.0;

  private final DatastoreService datastore;

  public GeoServlet() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  private static String getParameterOrDefault(
      HttpServletRequest req, String parameter, String defaultValue) {
    String value = req.getParameter(parameter);
    if (value == null || value.isEmpty()) {
      value = defaultValue;
    }
    return value;
  }

  private static GeoPt getOffsetPoint(
      GeoPt original, double latOffsetMeters, double lonOffsetMeters) {
    // Approximate the number of degrees to offset by meters.
    // http://gis.stackexchange.com/a/2964
    // How long (approximately) is one degree of longitude?
    double lonDegreeMeters = DEGREE_METERS * Math.cos(original.getLatitude());
    return new GeoPt(
        (float) (original.getLatitude() + latOffsetMeters / DEGREE_METERS),
        // This may cause errors if given points near the north or south pole.
        (float) (original.getLongitude() + lonOffsetMeters / lonDegreeMeters));
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    resp.setContentType("text/plain");
    resp.setCharacterEncoding("UTF-8");
    PrintWriter out = resp.getWriter();

    String brand = getParameterOrDefault(req, BRAND_PARAMETER, BRAND_DEFAULT);
    String latStr = getParameterOrDefault(req, LATITUDE_PARAMETER, LATITUDE_DEFAULT);
    String lonStr = getParameterOrDefault(req, LONGITUDE_PARAMETER, LONGITUDE_DEFAULT);
    String radiusStr = getParameterOrDefault(req, RADIUS_PARAMETER, RADIUS_DEFAULT);

    float latitude;
    float longitude;
    double r;
    try {
      latitude = Float.parseFloat(latStr);
      longitude = Float.parseFloat(lonStr);
      r = Double.parseDouble(radiusStr);
    } catch (IllegalArgumentException e) {
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST, String.format("Got bad value: %s", e.getMessage()));
      return;
    }

    // Get lat/lon for rectangle.
    GeoPt c = new GeoPt(latitude, longitude);
    GeoPt ne = getOffsetPoint(c, r, r);
    float neLat = ne.getLatitude();
    float neLon = ne.getLongitude();
    GeoPt sw = getOffsetPoint(c, -1 * r, -1 * r);
    float swLat = sw.getLatitude();
    float swLon = sw.getLongitude();

    // [START geospatial_stcontainsfilter_examples]
    // Testing for containment within a circle
    GeoPt center = new GeoPt(latitude, longitude);
    double radius = r; // Value is in meters.
    Filter f1 = new StContainsFilter("location", new Circle(center, radius));
    Query q1 = new Query("GasStation").setFilter(f1);

    // Testing for containment within a rectangle
    GeoPt southwest = new GeoPt(swLat, swLon);
    GeoPt northeast = new GeoPt(neLat, neLon);
    Filter f2 = new StContainsFilter("location", new Rectangle(southwest, northeast));
    Query q2 = new Query("GasStation").setFilter(f2);
    // [END geospatial_stcontainsfilter_examples]

    List<Entity> circleResults = datastore.prepare(q1).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d stations in %f meter radius circle.\n", circleResults.size(), radius);
    printStations(out, circleResults);
    out.println();

    List<Entity> rectResults = datastore.prepare(q2).asList(FetchOptions.Builder.withDefaults());
    out.printf("Got %d stations in rectangle.\n", rectResults.size());
    printStations(out, rectResults);
    out.println();

    List<Entity> brandResults = getStationsWithBrand(center, radius, brand);
    out.printf("Got %d stations in circle with brand %s.\n", brandResults.size(), brand);
    printStations(out, brandResults);
    out.println();
  }

  private void printStations(PrintWriter out, List<Entity> stations) {
    for (Entity station : stations) {
      GeoPt location = (GeoPt) station.getProperty(LOCATION_PROPERTY);
      out.printf(
          "%s: @%f, %f\n",
          (String) station.getProperty(BRAND_PROPERTY),
          location.getLatitude(),
          location.getLongitude());
    }
  }

  private List<Entity> getStationsWithBrand(GeoPt center, double radius, String value) {
    // [START geospatial_containment_and_equality_combination]
    Filter f =
        CompositeFilterOperator.and(
            new StContainsFilter("location", new Circle(center, radius)),
            new FilterPredicate("brand", FilterOperator.EQUAL, value));
    // [END geospatial_containment_and_equality_combination]
    Query q = new Query("GasStation").setFilter(f);
    return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
  }
}
