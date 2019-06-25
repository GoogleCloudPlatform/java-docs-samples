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
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsEnabled {
  // corsEnabled is an example of setting CORS headers.
  // For more information about CORS and CORS preflight requests, see
  // https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request.
  public void corsEnabled(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Set CORS headers for preflight requests
    if (request.getMethod().equals("OPTIONS")) {
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Methods", "POST");
      response.setHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setHeader("Access-Control-Max-Age", "3600");
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }

    // Set CORS headers for the main request.
    response.setHeader("Access-Control-Allow-Origin", "*");
    PrintWriter writer = response.getWriter();
    writer.write("CORS headers set successfully!");
  }
}

// [END functions_http_cors]
