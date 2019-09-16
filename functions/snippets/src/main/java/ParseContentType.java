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

// [START functions_http_content]

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ParseContentType {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  // Responds to an HTTP request using data from the request body parsed according to the
  // "content-type" header.
  public void parseContentType(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String name;
    String contentType = request.getContentType();
    if (contentType.equals("application/json")) {
      // '{"name":"John"}'
      JsonObject body = gsonParser.fromJson(request.getReader(), JsonObject.class);
      name = body.get("name").getAsString();
    } else if (contentType.equals("application/octet-stream")) {
      // 'John', stored in a Buffer
      name = new String(Base64.getDecoder().decode(request.getInputStream().readAllBytes()));
    } else if (contentType.equals("text/plain")) {
      // 'John'
      name = request.getReader().readLine();
    } else if (contentType.equals("application/x-www-form-urlencoded")) {
      // 'name=John' in the body of a POST request (not the URL)
      name = request.getParameter("name");
    } else {
      // Invalid or missing header "Content-Type"
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    PrintWriter writer = response.getWriter();
    writer.write(String.format("Hello %s!", name));
  }
}

// [END functions_http_content]
