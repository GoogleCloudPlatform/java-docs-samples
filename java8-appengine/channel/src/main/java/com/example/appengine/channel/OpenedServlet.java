/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.channel;

import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OpenedServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String gameId = req.getParameter("gamekey");
    Objectify ofy = ObjectifyService.ofy();
    try {
      Game game = ofy.load().type(Game.class).id(gameId).safe();
      if (gameId != null && req.getUserPrincipal() != null) {
        game.sendUpdateToClients();
        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
      } else {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }
    } catch (NotFoundException e) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
