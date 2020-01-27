/*
 * Copyright 2019 Google LLC
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

// [START functions_http_cors]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;

public class CorsEnabled implements HttpFunction {
  // corsEnabled is an example of setting CORS headers.
  // For more information about CORS and CORS preflight requests, see
  // https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    // Set CORS headers for preflight requests
    if (request.getMethod().equals("OPTIONS")) {
      response.appendHeader("Access-Control-Allow-Origin", "*");
      response.appendHeader("Access-Control-Allow-Methods", "POST");
      response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
      response.appendHeader("Access-Control-Max-Age", "3600");
      response.setStatusCode(204);
      return;
    }

    // Set CORS headers for the main request.
    response.appendHeader("Access-Control-Allow-Origin", "*");
    BufferedWriter writer = response.getWriter();
    writer.write("CORS headers set successfully!");
  }
}
// [END functions_http_cors]
