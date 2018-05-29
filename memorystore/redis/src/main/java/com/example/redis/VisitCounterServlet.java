/*
 * Copyright 2018 Google LLC
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
// [START memorystore_visit_servlet]

package com.example.redis;

import java.io.IOException;
import java.net.SocketException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@WebServlet(name = "Track visits", value = "")
public class VisitCounterServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JedisPool jedisPool = (JedisPool) req.getServletContext().getAttribute("jedisPool");

      if (jedisPool == null) {
        throw new SocketException("Error connecting to Jedis pool");
      }
      Long visits;

      try (Jedis jedis = jedisPool.getResource()) {
        visits = jedis.incr("visits");
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().println("Visitor counter: " + String.valueOf(visits));
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
// [END memorystore_visit_servlet]
