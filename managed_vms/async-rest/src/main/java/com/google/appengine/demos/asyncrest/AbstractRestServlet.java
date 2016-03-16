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
 * AbstractRestServlet.
 *
 */
public class AbstractRestServlet extends HttpServlet {

  protected final static int MAX_RESULTS = 5;

  protected final static String STYLE = "<style type='text/css'>"
          + "  img.thumb:hover {height:50px}"
          + "  img.thumb {vertical-align:text-top}"
          + "  span.red {color: #ff0000}"
          + "  span.green {color: #00ff00}"
          + "  iframe {border: 0px}" + "</style>";

  protected final static String APPKEY = "com.google.appengine.demos.asyncrest.appKey";
  protected final static String APPKEY_ENV = "PLACES_APPKEY";
  protected final static String LOC_PARAM = "loc";
  protected final static String ITEMS_PARAM = "items";
  protected final static String LATITUDE_PARAM = "lat";
  protected final static String LONGITUDE_PARAM = "long";
  protected final static String RADIUS_PARAM = "radius";
  protected String key;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    //first try the servlet context init-param
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

  public static String sanitize(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("<", "?").replace("&", "?").replace("\n", "?");
  }

  protected String restQuery(String coordinates, String radius, String item) {
    try {
      return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + key + "&location="
              + URLEncoder.encode(coordinates, "UTF-8") + "&types=" + URLEncoder.encode(item, "UTF-8")
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
      Map m = (Map) itor.next();
      String name = (String) m.get("name");
      Object[] photos = (Object[]) m.get("photos");
      if (photos != null && photos.length > 0) {
        resultCount++;
        thumbs.append("<img class='thumb' border='1px' height='40px'" + " src='"
                + getPhotoURL((String) (((Map) photos[0]).get("photo_reference"))) + "'" + " title='" + name
                + "'" + "/>");
        thumbs.append("</a>&nbsp;");
      }
    }
    return thumbs.toString();
  }

  public String getPhotoURL(String photoref) {
    return "https://maps.googleapis.com/maps/api/place/photo?key=" + key + "&photoreference=" + photoref
            + "&maxheight=40";
  }

  protected String ms(long nano) {
    BigDecimal dec = new BigDecimal(nano);
    return dec.divide(new BigDecimal(1000000L)).setScale(1, RoundingMode.UP).toString();
  }

  protected int width(long nano) {
    int w = (int) ((nano + 999999L) / 5000000L);
    if (w == 0) {
      w = 2;
    }
    return w;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    doGet(request, response);
  }

}
