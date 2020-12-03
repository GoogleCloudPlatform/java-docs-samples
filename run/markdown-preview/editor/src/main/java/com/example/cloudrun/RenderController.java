/*
 * Copyright 2020 Google LLC
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

package com.example.cloudrun;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RenderController {

  private static final Logger logger = LoggerFactory.getLogger(RenderController.class);

  // [START cloudrun_secure_request_do]
  // [START run_secure_request_do]
  // '/render' expects a JSON body payload with a 'data' property holding plain text
  // for rendering.
  @PostMapping(value = "/render", consumes = "application/json")
  public String render(@RequestBody Data data) {
    String markdown = data.getData();

    String url = System.getenv("EDITOR_UPSTREAM_RENDER_URL");
    if (url == null) {
      String msg =
          "No configuration for upstream render service: "
              + "add EDITOR_UPSTREAM_RENDER_URL environment variable";
      logger.error(msg);
      throw new IllegalStateException(msg);
    }

    String html = makeAuthenticatedRequest(url, markdown);
    return html;
  }
  // [END run_secure_request_do]
  // [END cloudrun_secure_request_do]

  // Instantiate OkHttpClient
  private static final OkHttpClient ok =
      new OkHttpClient.Builder()
          .readTimeout(500, TimeUnit.MILLISECONDS)
          .writeTimeout(500, TimeUnit.MILLISECONDS)
          .build();

  // [START cloudrun_secure_request]
  // [START run_secure_request]
  // makeAuthenticatedRequest creates a new HTTP request authenticated by a JSON Web Tokens (JWT)
  // retrievd from Application Default Credentials.
  public String makeAuthenticatedRequest(String url, String markdown) {
    String html = "";
    try {
      // Retrieve Application Default Credentials
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      IdTokenCredentials tokenCredentials =
          IdTokenCredentials.newBuilder()
              .setIdTokenProvider((IdTokenProvider) credentials)
              .setTargetAudience(url)
              .build();

      // Create an ID token
      String token = tokenCredentials.refreshAccessToken().getTokenValue();
      // Instantiate HTTP request
      MediaType contentType = MediaType.get("text/plain; charset=utf-8");
      okhttp3.RequestBody body = okhttp3.RequestBody.create(markdown, contentType);
      Request request =
          new Request.Builder()
              .url(url)
              .addHeader("Authorization", "Bearer " + token)
              .post(body)
              .build();

      Response response = ok.newCall(request).execute();
      html = response.body().string();
    } catch (IOException e) {
      logger.error("Unable to get rendered data", e);
    }
    return html;
  }
  // [END run_secure_request]
  // [END cloudrun_secure_request]
}
