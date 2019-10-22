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

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket server example using JSR-356 Java WebSocket API. Echoes back the message received over
 * the websocket back to the client.
 */
@ServerEndpoint("/echo")
public class ServerSocket {

  private static final Logger logger = Logger.getLogger(ServerSocket.class.getName());
  private static final String ENDPOINT = "/echo";
  private static final String WEBSOCKET_PROTOCOL_PREFIX = "ws://";
  private static final String WEBSOCKET_HTTPS_PROTOCOL_PREFIX = "wss://";
  private static final String APPENGINE_HOST_SUFFIX = ".appspot.com";
  // GAE_INSTANCE environment is used to detect App Engine Flexible Environment
  private static final String GAE_INSTANCE_VAR = "GAE_INSTANCE";
  // GOOGLE_CLOUD_PROJECT environment variable is set to the GCP project ID on App Engine Flexible.
  private static final String GOOGLE_CLOUD_PROJECT_ENV_VAR = "GOOGLE_CLOUD_PROJECT";
  // GAE_SERVICE environment variable is set to the GCP service name.
  private static final String GAE_SERVICE_ENV_VAR = "GAE_SERVICE";

  @OnOpen
  public void onOpen(Session session) {
    logger.info("WebSocket Opened: " + session.getId());
  }

  /**
   * Handle a message received from the client, and echo back to the client.
   *
   * @param message Message in text format
   * @param session Current active session
   * @throws IOException error sending message back to client
   */
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    logger.fine("Message Received  : " + message);
    // echo message back to the client
    session.getAsyncRemote().sendText(message);
  }

  @OnClose
  public void onClose(CloseReason reason, Session session) {
    logger.fine("Closing WebSocket: " + reason.getReasonPhrase());
  }

  /**
   * Returns the host:port/echo address a client needs to use to communicate with the server. On App
   * engine Flex environments, result will be in the form wss://project-id.appspot.com/echo
   */
  public static String getWebSocketAddress() {
    // Use ws://127.0.0.1:8080/echo when testing locally
    String webSocketHost = "127.0.0.1:8080";
    String webSocketProtocolPrefix = WEBSOCKET_PROTOCOL_PREFIX;

    // On App Engine flexible environment, use wss://project-id.appspot.com/echo
    if (System.getenv(GAE_INSTANCE_VAR) != null) {
      String projectId = System.getenv(GOOGLE_CLOUD_PROJECT_ENV_VAR);
      if (projectId != null) {
        String serviceName = System.getenv(GAE_SERVICE_ENV_VAR);
        webSocketHost = serviceName + "-dot-" + projectId + APPENGINE_HOST_SUFFIX;
      }
      Preconditions.checkNotNull(webSocketHost);
      // Use wss:// instead of ws:// protocol when connecting over https
      webSocketProtocolPrefix = WEBSOCKET_HTTPS_PROTOCOL_PREFIX;
    }
    return webSocketProtocolPrefix + webSocketHost + ENDPOINT;
  }
}
