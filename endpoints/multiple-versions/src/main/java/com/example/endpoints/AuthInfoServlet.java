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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that returns authentication information.
 * See openapi.yaml for authentication mechanisms (e.g. JWT tokens, Google ID token).
 */
@WebServlet("/auth/info/*")
public class AuthInfoServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String encodedInfo = req.getHeader("X-Endpoint-API-UserInfo");
    if (encodedInfo == null || encodedInfo == "") {
      JsonObject anon = new JsonObject();
      anon.addProperty("id", "anonymous");
      new Gson().toJson(anon, resp.getWriter());
      return;
    }

    try {
      byte[] authInfo = Base64.getDecoder().decode(encodedInfo);
      resp.getOutputStream().write(authInfo);
    } catch (IllegalArgumentException iae) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JsonObject error = new JsonObject();
      error.addProperty("code", HttpServletResponse.SC_BAD_REQUEST);
      error.addProperty("message", "Could not decode auth info.");
      new Gson().toJson(error, resp.getWriter());
    }
  }
}
