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

package functions;

// [START functions_http_cors_auth]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;

public class CorsEnabledAuth implements HttpFunction {
  // corsEnabledAuth is an example of setting CORS headers.
  // For more information about CORS and CORS preflight requests, see
  // https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    // Set CORS headers
    //   Allows GETs from origin https://mydomain.com
    //   with the Authorization header present
    response.appendHeader("Access-Control-Allow-Origin", "https://mydomain.com");
    response.appendHeader("Access-Control-Allow-Credentials", "true");

    if ("OPTIONS".equals(request.getMethod())) {
      response.appendHeader("Access-Control-Allow-Methods", "GET");
      response.appendHeader("Access-Control-Allow-Headers", "Authorization");
      response.appendHeader("Access-Control-Max-Age", "3600");
      response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
      return;
    }

    // Handle the main request
    BufferedWriter writer = response.getWriter();
    writer.write("CORS headers set successfully!");
  }
}
// [END functions_http_cors_auth]
