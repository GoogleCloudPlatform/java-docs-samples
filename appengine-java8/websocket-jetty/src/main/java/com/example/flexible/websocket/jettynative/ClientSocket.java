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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Basic Echo Client Socket.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class ClientSocket {
  private Logger logger = Logger.getLogger(ClientSocket.class.getName());
  private Session session;
  // stores the messages in-memory.
  // Note : this is currently an in-memory store for demonstration,
  // not recommended for production use-cases.
  private static Collection<String> messages = new ConcurrentLinkedDeque<>();

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    logger.fine("Connection closed: " + statusCode + ":" + reason);
    this.session = null;
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    this.session = session;
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    logger.fine("Message Received : " + msg);
    messages.add(msg);
  }

  // Retrieve all received messages.
  public static Collection<String> getReceivedMessages() {
    return Collections.unmodifiableCollection(messages);
  }
}
