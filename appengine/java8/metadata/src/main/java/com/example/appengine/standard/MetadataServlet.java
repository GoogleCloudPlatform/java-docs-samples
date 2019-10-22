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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;


// [START example]
@SuppressWarnings("serial")
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(name = "Metadata", description = "Metadata: Write info about GAE Standard",
    urlPatterns = "/metadata")
public class MetadataServlet extends HttpServlet {

  private final String[] metaPath = {
      "/computeMetadata/v1/project/numeric-project-id", //  (pending)
      "/computeMetadata/v1/project/project-id",
      "/computeMetadata/v1/instance/zone",
      "/computeMetadata/v1/instance/service-accounts/default/aliases",
      "/computeMetadata/v1/instance/service-accounts/default/email",
      "/computeMetadata/v1/instance/service-accounts/default/",
      "/computeMetadata/v1/instance/service-accounts/default/scopes",
      // Tokens work - but are a security risk to display
      //      "/computeMetadata/v1/instance/service-accounts/default/token"
  };

  final String[] metaServiceAcct = {
      "/computeMetadata/v1/instance/service-accounts/{account}/aliases",
      "/computeMetadata/v1/instance/service-accounts/{account}/email",
      "/computeMetadata/v1/instance/service-accounts/{account}/scopes",
      // Tokens work - but are a security risk to display
      //     "/computeMetadata/v1/instance/service-accounts/{account}/token"
  };

  private final String metadata = "http://metadata.google.internal";
  private TemplateEngine templateEngine;

  // Use OkHttp from Square as it's quite easy to use for simple fetches.
  private final OkHttpClient ok = new OkHttpClient.Builder()
      .readTimeout(500, TimeUnit.MILLISECONDS)  // Don't dawdle
      .writeTimeout(500, TimeUnit.MILLISECONDS)
      .build();

  // Setup to pretty print returned json
  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .create();
  private final JsonParser jp = new JsonParser();

  // Fetch Metadata
  String fetchMetadata(String key) throws IOException {
    Request request = new Request.Builder()
        .url(metadata + key)
        .addHeader("Metadata-Flavor", "Google")
        .get()
        .build();

    Response response = ok.newCall(request).execute();
    return response.body().string();
  }

  String fetchJsonMetadata(String prefix) throws IOException {
    Request request = new Request.Builder()
        .url(metadata + prefix)
        .addHeader("Metadata-Flavor", "Google")
        .get()
        .build();

    Response response = ok.newCall(request).execute();

    // Convert json to prety json
    return gson.toJson(jp.parse(response.body().string()));
  }

  @Override
  public void init() {
    // Setup ThymeLeaf
    ServletContextTemplateResolver templateResolver =
        new ServletContextTemplateResolver(this.getServletContext());

    templateResolver.setPrefix("/WEB-INF/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCacheTTLMs(Long.valueOf(1200000L)); // TTL=20m

    // Cache is set to true by default. Set to false if you want templates to
    // be automatically updated when modified.
    templateResolver.setCacheable(true);

    templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String defaultServiceAccount = "";
    WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());

    resp.setContentType("text/html");

    String environment =
        (String) System.getProperties().get("com.google.appengine.runtime.environment");
    ctx.setVariable("production", environment);

    // The metadata server is only on a production system
    if (environment.equals("Production")) {

      TreeMap<String, String> m = new TreeMap<>();

      for (String key : metaPath) {
        m.put(key, fetchMetadata(key));
        if (key.contains("default/email")) {
          defaultServiceAccount = m.get(key);
        }
      }

      ctx.setVariable("Metadata", m.descendingMap());

      m = new TreeMap<>();
      for (String key : metaServiceAcct) {
        // substitute a service account for {account}
        key = key.replace("{account}", defaultServiceAccount);
        m.put(key, fetchMetadata(key));
      }
      ctx.setVariable("sam", m.descendingMap());

      // Recursivly get all info about service accounts -- Note tokens are leftout by default.
      ctx.setVariable("rsa",
          fetchJsonMetadata("/computeMetadata/v1/instance/service-accounts/?recursive=true"));
      // Recursivly get all data on Metadata server.
      ctx.setVariable("ram", fetchJsonMetadata("/?recursive=true"));
    }

    templateEngine.process("index", ctx, resp.getWriter());

  }
}
// [END example]
