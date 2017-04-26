/**
 * Copyright 2015 Google Inc.
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

package com.example.endpoints;

import com.example.endpoints.message.Message;
import com.example.endpoints.message.MessageTranslator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Version agnostic echo. */
public class Echo {

  public static void echo(
      MessageTranslator translator, HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.addHeader("Content-Encoding", "application/json");

    try {
      JsonReader jsonReader = new JsonReader(req.getReader());
      Message message =
          translator.fromExternalToInternal(new Gson().fromJson(jsonReader, Map.class));
      performTask(message);
      new Gson().toJson(translator.fromInternalToExternal(message), resp.getWriter());
    } catch (JsonParseException je) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JsonObject error = new JsonObject();
      error.addProperty("code", HttpServletResponse.SC_BAD_REQUEST);
      error.addProperty("message", "Body was not valid JSON.");
      new Gson().toJson(error, resp.getWriter());
    }
  }

  private static void performTask(Message message) {
    message.setMessage(message.getMessage().toUpperCase());
  }
}
