/*
 * Copyright 2017 Google Inc.
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


package com.example.appengine.standard;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//CHECKSTYLE.OFF: AbbreviationAsWordInName - readability
// [START example]
@SuppressWarnings({"serial"})
public class GAEInfoServlet extends HttpServlet {

  public void table(PrintWriter p, String title, String c) {
    p.print("<h3>" + title + "</h3>");
    p.print("<table>");
    p.print(c);
    p.print("</table>");
  }

  public String tr(String c) {
    return "<tr>" + c + "</tr>";
  }

  public String td(String s) {
    return "<td>" + s + "</td>";
  }


  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    PrintWriter p = resp.getWriter();


    p.print("<html><body>");

    final AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
    table(
        p, "AppIdentity",
        tr(td("ServiceAccountName") + td(appIdentity.getServiceAccountName()))
            + tr(td("GCS Bucket") + td(appIdentity.getDefaultGcsBucketName()))
    );

    table(p, "SystemProperties",
        tr(td("appId") + td(SystemProperty.applicationId.get()))
            + tr(td("appVer") + td(SystemProperty.applicationVersion.get()))
            + tr(td("version") + td(SystemProperty.version.get()))
            + tr(td("environment") + td(SystemProperty.environment.get()))
    );


    // Environment Atributes
    ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
    Map<String,Object> attr = env.getAttributes();

    String c = "";
    for (String key : attr.keySet()) {
      Object o = attr.get(key);

      if (o.getClass().getCanonicalName().equals("java.lang.String")) {
        c += tr(td(key) + td((String) o));
      } else {
        c += tr(td(key) + td(o.getClass().getCanonicalName()));
      }
    }
    table(p, "Environment Attributes", c);

    c = "";
    for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String val = req.getHeader(key);
      c += tr(td(key) + td(val));;
    }
    table(p, "Headers", c);


    Cookie[] cookies = req.getCookies();
    if (cookies != null && cookies.length != 0) {
      c = "";
      for (Cookie co : cookies) {
        c += tr(td(co.getName()) + td(co.getValue()) + td(co.getComment())
            + td(co.getPath()) + td(Integer.toString(co.getMaxAge())));
      }
      table(p, "Cookies", c);
    }

    Properties properties = System.getProperties();
    c = "";
    for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      c += tr(td(key) + td((String)properties.get(key)));
    }
    table(p, "Java SystemProperties", c);

    Map<String, String> envVar = System.getenv();
    c = "";
    for (String key : envVar.keySet()) {
      c += tr(td(key) + td(envVar.get(key)));
    }
    table(p, "Envirionment Variables", c);
    p.print("</body></html>");
    p.close();

  }
}
// [END example]
//CHECKSTYLE.ON: AbbreviationAsWordInName