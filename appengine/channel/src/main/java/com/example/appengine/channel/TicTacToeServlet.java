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

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TicTacToeServlet extends HttpServlet {
  private String getGameUriWithGameParam(HttpServletRequest req, String gameKey)
      throws IOException {
    try {
      String query;
      if (gameKey == null) {
        query = "";
      } else {
        query = "g=" + gameKey;
      }
      URI thisUri = new URI(req.getRequestURL().toString());
      URI uriWithOptionalGameParam =
          new URI(thisUri.getScheme(), thisUri.getUserInfo(), thisUri.getHost(),
              thisUri.getPort(), thisUri.getPath(), query, "");
      return uriWithOptionalGameParam.toString();
    } catch (URISyntaxException e) {
      throw new IOException(e.getMessage(), e);
    }

  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final URI uriWithOptionalGameParam;
    String gameKey = req.getParameter("g");
    if (userService.getCurrentUser() == null) {
      resp.getWriter().println("<p>Please <a href=\""
          + userService.createLoginURL(getGameUriWithGameParam(req, gameKey))
          + "\">sign in</a>.</p>");

      return;
    }

    Objectify ofy = ObjectifyService.ofy();
    Game game = null;
    String userId = userService.getCurrentUser().getUserId();
    if (gameKey != null) {
      game = ofy.load().type(Game.class).id(gameKey).safe();
      if (game.getUserO() == null && !userId.equals(game.getUserX())) {
        game.setUserO(userId);
      }
      ofy.save().entity(game).now();
    } else {
      game = new Game(userId, null, "         ", true);
      ofy.save().entity(game).now();
      gameKey = game.getId();
    }

    //[START channel_service]
    ChannelService channelService = ChannelServiceFactory.getChannelService();

    // The 'Game' object exposes a method which creates a unique string based on the game's key
    // and the user's id.
    String token = channelService.createChannel(game.getChannelKey(userId));

    //[END channel_service]

    ofy.save().entity(game).now();

    req.setAttribute("game_key", gameKey);
    req.setAttribute("me", userId);
    req.setAttribute("token", token);
    req.setAttribute("initial_message", game.getMessageString());
    req.setAttribute("game_link", getGameUriWithGameParam(req, gameKey));
    getServletContext().getRequestDispatcher("/WEB-INF/view/index.jsp").forward(req, resp);
  }
}
