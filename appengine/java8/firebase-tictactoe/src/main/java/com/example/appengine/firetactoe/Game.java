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

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The datastore-persisted Game object. This holds the entire game state - from a representation of
 * the board, to the players are and whose turn it is, and who the winner is and how they won.
 *
 * <p>It also contains some convenience functions for communicating updates to the board to the
 * clients, via Firebase.
 */
@Entity
public class Game {

  static final Pattern[] XWins = {
      Pattern.compile("XXX......"),
      Pattern.compile("...XXX..."),
      Pattern.compile("......XXX"),
      Pattern.compile("X..X..X.."),
      Pattern.compile(".X..X..X."),
      Pattern.compile("..X..X..X"),
      Pattern.compile("X...X...X"),
      Pattern.compile("..X.X.X..")
  };
  static final Pattern[] OWins = {
      Pattern.compile("OOO......"),
      Pattern.compile("...OOO..."),
      Pattern.compile("......OOO"),
      Pattern.compile("O..O..O.."),
      Pattern.compile(".O..O..O."),
      Pattern.compile("..O..O..O"),
      Pattern.compile("O...O...O"),
      Pattern.compile("..O.O.O..")
  };

  @Id
  public String id;
  public String userX;
  public String userO;
  public String board;
  public Boolean moveX;
  public String winner;
  public String winningBoard;

  private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

  Game() {
    this.id = UUID.randomUUID().toString();
  }

  Game(String userX, String userO, String board, boolean moveX) {
    this.id = UUID.randomUUID().toString();
    this.userX = userX;
    this.userO = userO;
    this.board = board;
    this.moveX = moveX;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserX() {
    return userX;
  }

  public String getUserO() {
    return userO;
  }

  public void setUserO(String userO) {
    this.userO = userO;
  }

  public String getBoard() {
    return board;
  }

  public void setBoard(String board) {
    this.board = board;
  }

  public boolean getMoveX() {
    return moveX;
  }

  public void setMoveX(boolean moveX) {
    this.moveX = moveX;
  }

  // [START send_updates]
  public String getChannelKey(String userId) {
    return userId + id;
  }

  /**
   * deleteChannel.
   * @param userId .
   * @throws IOException .
   */
  public void deleteChannel(String userId) throws IOException {
    if (userId != null) {
      String channelKey = getChannelKey(userId);
      FirebaseChannel.getInstance().sendFirebaseMessage(channelKey, null);
    }
  }

  private void sendUpdateToUser(String userId) throws IOException {
    if (userId != null) {
      String channelKey = getChannelKey(userId);
      FirebaseChannel.getInstance().sendFirebaseMessage(channelKey, this);
    }
  }

  /**
   * sendUpdateToClients.
   * @throws IOException if we had some kind of network issue.
   */
  public void sendUpdateToClients() throws IOException {
    sendUpdateToUser(userX);
    sendUpdateToUser(userO);
  }
  // [END send_updates]

  /**
   * checkWin - has anyone won.
   */
  public void checkWin() {
    final Pattern[] wins;
    if (moveX) {
      wins = XWins;
    } else {
      wins = OWins;
    }

    for (Pattern winPattern : wins) {
      if (winPattern.matcher(board).matches()) {
        if (moveX) {
          winner = userX;
        } else {
          winner = userO;
        }
        winningBoard = winPattern.toString();
      }
    }
  }

  /**
   * makeMove for user.
   * @param position .
   * @param userId .
   * @return true if successful.
   */
  public boolean makeMove(int position, String userId) {
    String currentMovePlayer;
    char value;
    if (getMoveX()) {
      value = 'X';
      currentMovePlayer = getUserX();
    } else {
      value = 'O';
      currentMovePlayer = getUserO();
    }

    if (currentMovePlayer.equals(userId)) {
      char[] boardBytes = getBoard().toCharArray();
      boardBytes[position] = value;
      setBoard(new String(boardBytes));
      checkWin();
      setMoveX(!getMoveX());
      try {
        sendUpdateToClients();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Error sending Game update to Firebase", e);
        throw new RuntimeException(e);
      }
      return true;
    }

    return false;
  }
}
