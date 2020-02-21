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

package com.example.functions;

// [START functions_helloworld_http]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class HelloHttp implements HttpFunction {
  private static final Logger LOGGER = Logger.getLogger(HelloHttp.class.getName());

  private Gson gsonParser = new Gson();

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    String name = "world";

    // Check URL parameters for "name" field
    if (request.getFirstQueryParameter("name").isPresent()) {
      name = request.getFirstQueryParameter("name").get();
    }

    // Parse JSON request and check for "name" field
    try {
      JsonElement requestParsed = gsonParser.fromJson(request.getReader(), JsonElement.class);
      JsonObject requestJson = null;

      if (requestParsed.isJsonObject()) {
        requestJson = requestParsed.getAsJsonObject();
      }

      if (requestJson != null && requestJson.has("name")) {
        name = requestJson.get("name").getAsString();
      }
    } catch (JsonParseException e) {
      LOGGER.severe("Error parsing JSON: " + e.getMessage());
    }

    BufferedWriter writer = response.getWriter();
    writer.write(String.format("Hello %s!", name));
  }
}
// [END functions_helloworld_http]