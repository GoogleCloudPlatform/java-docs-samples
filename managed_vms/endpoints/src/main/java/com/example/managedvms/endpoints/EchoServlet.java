/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.endpoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

/**
 * A servlet that echoes JSON message bodies.
 */
@WebServlet("/echo")
public class EchoServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader("Content-Encoding", "application/json");

    Object responseBody;
    try {
      JsonReader jsonReader = new JsonReader(req.getReader());
      responseBody = new Gson().fromJson(jsonReader, Map.class);
    } catch (JsonParseException je) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JsonObject error = new JsonObject();
      error.addProperty("code", HttpServletResponse.SC_BAD_REQUEST);
      error.addProperty("message", "Body was not valid JSON.");
      responseBody = error;
    }

    new Gson().toJson(responseBody, resp.getWriter());
  }
}
