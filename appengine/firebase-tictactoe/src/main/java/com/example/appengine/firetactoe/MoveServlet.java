/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.firetactoe;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler for a user making a move in a game.
 * Updates the game board with the requested move (if it's legal), and communicate the updated board
 * to the clients.
 */
public class MoveServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String gameId = request.getParameter("gameKey");
    Objectify ofy = ObjectifyService.ofy();
    Game game = ofy.load().type(Game.class).id(gameId).safe();

    UserService userService = UserServiceFactory.getUserService();
    String currentUserId = userService.getCurrentUser().getUserId();

    int cell = new Integer(request.getParameter("cell"));
    if (!game.makeMove(getServletContext(), cell, currentUserId)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      ofy.save().entity(game).now();
    }
  }
}
