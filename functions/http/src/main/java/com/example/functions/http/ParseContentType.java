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

package com.example.functions.http;

// [START functions_http_content]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.Optional;

public class ParseContentType implements HttpFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  // Responds to an HTTP request using data from the request body parsed according to the
  // "content-type" header.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    String name = null;
    String contentType = request.getContentType().get();
    switch (contentType) {
      case "application/json":
        // '{"name":"John"}'
        JsonObject body = gsonParser.fromJson(request.getReader(), JsonObject.class);
        if (body.has("name")) {
          name = body.get("name").getAsString();
          break;
        }
        // else: No "name" parameter specified; fall through to default case
      case "application/octet-stream":
        // 'John', stored in a Buffer
        name = new String(Base64.getDecoder().decode(request.getInputStream().readAllBytes()));
        break;
      case "text/plain":
        // 'John'
        name = request.getReader().readLine();
        break;
      case "application/x-www-form-urlencoded":
        // 'name=John' in the body of a POST request (not the URL)
        Optional<String> nameParam = request.getFirstQueryParameter("name");
        if (nameParam.isPresent()) {
          name = nameParam.get();
          break;
        }
        // else: No "name" parameter specified; fall through to default case
    default:
      // Invalid or missing header "Content-Type"
      response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
      return;
    }

    // Respond with a name, if one was detected
    if (name != null) {
      BufferedWriter writer = response.getWriter();
      writer.write(String.format("Hello %s!", name));
    }
  }
}
// [END functions_http_content]
