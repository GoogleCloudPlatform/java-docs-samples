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

package com.example.flexible.websocket.jsr356;

import com.google.common.util.concurrent.SettableFuture;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Web socket client example using JSR-356 Java WebSocket API. Sends a message to the server, and
 * stores the echoed messages received from the server.
 */
@ClientEndpoint
public class ClientSocket {

  private static final Logger logger = Logger.getLogger(ClientSocket.class.getName());

  // stores the messages in-memory.
  // Note : this is currently an in-memory store for demonstration,
  // not recommended for production use-cases.
  private static Collection<String> messages = new ConcurrentLinkedDeque<>();

  private SettableFuture<Boolean> future = SettableFuture.create();
  private Session session;

  ClientSocket(URI endpointUri) {
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      session = container.connectToServer(this, endpointUri);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @OnOpen
  public void onOpen(Session session) {
    future.set(true);
  }

  /**
   * Handles message received from the server.
   * @param message server message in String format
   * @param session current session
   */
  @OnMessage
  public void onMessage(String message, Session session) {
    logger.fine("Received message from server : " + message);
    messages.add(message);
  }

  boolean waitOnOpen() throws InterruptedException, ExecutionException {
    // wait on handling onOpen
    boolean opened = future.get();
    logger.fine("Connected to server");
    return opened;
  }

  @OnClose
  public void onClose(CloseReason reason, Session session) {
    logger.fine("Closing Web Socket: " + reason.getReasonPhrase());
  }

  void sendMessage(String str) {
    try {
      // Send a message to the server
      logger.fine("Sending message : " + str);
      session.getAsyncRemote().sendText(str);
    } catch (Exception e) {
      logger.severe("Error sending message : " + e.getMessage());
    }
  }

  // Retrieve all received messages.
  public static Collection<String> getReceivedMessages() {
    return Collections.unmodifiableCollection(messages);
  }

  @OnError
  public void logErrors(Throwable t) {
    logger.severe(t.getMessage());
  }
}
