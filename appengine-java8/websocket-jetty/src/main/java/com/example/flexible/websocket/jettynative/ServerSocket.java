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

package com.example.flexible.websocket.jettynative;

import java.io.IOException;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/*
 * Server-side WebSocket : echoes received message back to client.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class ServerSocket {
  private Logger logger = Logger.getLogger(SendServlet.class.getName());
  private Session session;

  @OnWebSocketConnect
  public void onWebSocketConnect(Session session) {
    this.session = session;
    logger.fine("Socket Connected: " + session);
  }

  @OnWebSocketMessage
  public void onWebSocketText(String message) {
    logger.fine("Received message: " + message);
    try {
      // echo message back to client
      this.session.getRemote().sendString(message);
    } catch (IOException e) {
      logger.severe("Error echoing message: " + e.getMessage());
    }
  }

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    logger.fine("Socket Closed: [" + statusCode + "] " + reason);
  }

  @OnWebSocketError
  public void onWebSocketError(Throwable cause) {
    logger.severe("Websocket error : " + cause.getMessage());
  }
}
