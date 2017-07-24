/**
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.appengine.standard;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// [START example]
@SuppressWarnings({"serial"})
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(name = "GAEInfo", description = "GAEInfo: Write info about GAE Standard",
    urlPatterns = "/gaeinfo")
public class GaeInfoServlet extends HttpServlet {

  final String[] v1 = {
      "/computeMetadata/v1/project/numeric-project-id", //  (pending)
      "/computeMetadata/v1/project/project-id",
      "/computeMetadata/v1/instance/zone",
      "/computeMetadata/v1/instance/service-accounts/default/aliases",
      "/computeMetadata/v1/instance/service-accounts/default/",
      "/computeMetadata/v1/instance/service-accounts/default/scopes",
// Tokens work - but are a security risk to display
//      "/computeMetadata/v1/instance/service-accounts/default/token"
  };

  final String[] v1Acct = {
      "/computeMetadata/v1/instance/service-accounts/{account}/aliases",
      "/computeMetadata/v1/instance/service-accounts/{account}/email",
      "/computeMetadata/v1/instance/service-accounts/{account}/scopes",
// Tokens work - but are a security risk to display
//     "/computeMetadata/v1/instance/service-accounts/{account}/token"
  };

  final String metadata = "http://metadata.google.internal";

  public final OkHttpClient ok = new OkHttpClient.Builder()
      .readTimeout(500, TimeUnit.MILLISECONDS)  // Don't dawdle
      .writeTimeout(500, TimeUnit.MILLISECONDS)
      .build();

  public Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .create();
  public JsonParser jp = new JsonParser();

  StringBuilder table(String title, String c) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h3>");
    sb.append(title);
    sb.append("</h3><table>");
    sb.append(c);
    sb.append("</table>");
    return sb;
  }

  String tr(String c) {
    return "<tr>" + c + "</tr>";
  }

  String td(String s) {
    return "<td>" + s + "</td>";
  }

  String fetchMetadata(String key) throws IOException {
    Request request = new Request.Builder()
        .url(metadata + key)
        .addHeader("Metadata-Flavor", "Google")
        .get()
        .build();

    Response response = ok.newCall(request).execute();
    return response.body().string();
  }

  String recursMetadata(String prefix) throws IOException {
    Request request = new Request.Builder()
        .url(metadata + prefix + "?recursive=true")
        .addHeader("Metadata-Flavor", "Google")
        .get()
        .build();

    Response response = ok.newCall(request).execute();

    return gson.toJson(jp.parse(response.body().string()));
  }


  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    PrintWriter p = resp.getWriter();
    StringBuilder sb = new StringBuilder(4096);

    p.print("<html><body>");

    final AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();

    sb.append(table("AppIdentity",
        tr(td("ServiceAccountName") + td(appIdentity.getServiceAccountName()))
        + tr(td("GCS Bucket") + td(appIdentity.getDefaultGcsBucketName()))
    ));

    sb.append(table("SystemProperties",
        tr(td("appId") + td(SystemProperty.applicationId.get()))
            + tr(td("appVer") + td(SystemProperty.applicationVersion.get()))
            + tr(td("version") + td(SystemProperty.version.get()))
            + tr(td("environment") + td(SystemProperty.environment.get()))
    ));

    // Environment Atributes
    ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
    Map<String, Object> attr = env.getAttributes();

    StringBuilder c = new StringBuilder(1024);
    for (String key : attr.keySet()) {
      Object o = attr.get(key);

      if (o.getClass().getCanonicalName().equals("java.lang.String")) {
        c.append(tr(td(key) + td((String) o)));
      } else {
        c.append(tr(td(key) + td(o.getClass().getCanonicalName())));
      }
    }
    sb.append(table("Environment Attributes", c.toString()));

    c = new StringBuilder(1024);
    for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements(); ) {
      String key = e.nextElement();
      String val = req.getHeader(key);
      c.append(tr(td(key) + td(val)));
    }
    sb.append(table("Headers", c.toString()));

    Cookie[] cookies = req.getCookies();
    if (cookies != null && cookies.length != 0) {
      c = new StringBuilder();
      for (Cookie co : cookies) {
        c.append(tr(td(co.getName()) + td(co.getValue()) + td(co.getComment())
            + td(co.getPath()) + td(Integer.toString(co.getMaxAge()))));
      }
      sb.append(table("Cookies", c.toString()));
    }

    Properties properties = System.getProperties();
    c = new StringBuilder(1024);
    for (Enumeration e = properties.propertyNames(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      c.append(tr(td(key) + td((String) properties.get(key))));
    }
    sb.append(table("Java SystemProperties", c.toString()));

    Map<String, String> envVar = System.getenv();
    c = new StringBuilder(1024);
    for (String key : envVar.keySet()) {
      c.append(tr(td(key) + td(envVar.get(key))));
    }
    sb.append(table("Envirionment Variables", c.toString()));

    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
      c = new StringBuilder(1024);
      for (String key : v1) {
        String val = fetchMetadata(key);
        if (val != null) {
          c.append(tr(td(key) + td(val)));
        }
      }
      sb.append(table("Metadata", c.toString()));

      c = new StringBuilder(1024);
      for (String key : v1Acct) {
        key = key.replace("{account}", appIdentity.getServiceAccountName());
        String val = fetchMetadata(key);
        if (val != null) {
          c.append(tr(td(key) + td(val)));
        }
      }
      sb.append(table("ServiceAccount Metadata", c.toString()));

      sb.append("<h3>Recursive service-accounts</h3><pre><code>"
          + recursMetadata("/computeMetadata/v1/instance/service-accounts/")
          + "</code></pre>");
      sb.append("<h3>Recursive all metadata</h3><pre><code>"
          + recursMetadata("/")
          + "</code></pre>");
    }

    sb.append("</body></html>");
    p.append(sb);
    p.close();

  }
}
// [END example]
