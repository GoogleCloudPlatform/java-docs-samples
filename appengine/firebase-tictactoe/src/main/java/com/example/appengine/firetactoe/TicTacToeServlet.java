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

import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base handler for the Tic Tac Toe game.
 * This handler serves up the initial jsp page that is the game, and also creates the persistent
 * game in the datastore, as well as the Firebase database to serve as the communication channel to
 * the clients.
 */
@SuppressWarnings("serial")
public class TicTacToeServlet extends HttpServlet {

  private String getGameUriWithGameParam(HttpServletRequest request, String gameKey) {
    try {
      String query = "";
      if (gameKey != null) {
        query = "gameKey=" + gameKey;
      }
      URI thisUri = new URI(request.getRequestURL().toString());
      URI uriWithOptionalGameParam = new URI(
          thisUri.getScheme(), thisUri.getUserInfo(), thisUri.getHost(),
          thisUri.getPort(), thisUri.getPath(), query, "");
      return uriWithOptionalGameParam.toString();
    } catch (URISyntaxException e) {
      // This should never happen, since we're constructing the URI from a valid URI.
      // Nonetheless, wrap it in a RuntimeException to placate java.
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String gameKey = request.getParameter("gameKey");

    // 1. Create or fetch a Game object from the datastore
    Objectify ofy = ObjectifyService.ofy();
    Game game = null;
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    if (gameKey != null) {
      game = ofy.load().type(Game.class).id(gameKey).now();
      if (null == game) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      if (game.getUserO() == null && !userId.equals(game.getUserX())) {
        game.setUserO(userId);
      }
      ofy.save().entity(game).now();
    } else {
      // Initialize a new board. The board is represented as a String of 9 spaces, one for each
      // blank spot on the tic-tac-toe board.
      game = new Game(userId, null, "         ", true);
      ofy.save().entity(game).now();
      gameKey = game.getId();
    }

    // 2. Create this Game in the firebase db
    game.sendUpdateToClients(getServletContext());

    // 3. Inject a secure token into the client, so it can get game updates

    // [START pass_token]
    // The 'Game' object exposes a method which creates a unique string based on the game's key
    // and the user's id.
    String token = FirebaseChannel.getInstance(getServletContext())
        .createFirebaseToken(game, userId);
    request.setAttribute("token", token);

    // 4. More general template values
    request.setAttribute("game_key", gameKey);
    request.setAttribute("me", userId);
    request.setAttribute("channel_id", game.getChannelKey(userId));
    request.setAttribute("initial_message", new Gson().toJson(game));
    request.setAttribute("game_link", getGameUriWithGameParam(request, gameKey));
    request.getRequestDispatcher("/WEB-INF/view/index.jsp").forward(request, response);
    // [END pass_token]
  }
}
