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

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.scopes.SimpleContainerScope;

@WebServlet("/send")
/** Servlet that sends the message sent over POST to over a websocket connection. */
public class SendServlet extends HttpServlet {

  private Logger logger = Logger.getLogger(SendServlet.class.getName());

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

  private final WebSocketClient webSocketClient;
  private final ClientSocket clientSocket;

  public SendServlet() {
    this.webSocketClient = createWebSocketClient();
    this.clientSocket = new ClientSocket();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String message = request.getParameter("message");
    try {
      sendMessageOverWebSocket(message);
      response.sendRedirect("/");
    } catch (Exception e) {
      logger.severe("Error sending message over socket: " + e.getMessage());
      e.printStackTrace(response.getWriter());
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
  }

  private WebSocketClient createWebSocketClient() {
    WebSocketClient webSocketClient;
    if (System.getenv(GAE_INSTANCE_VAR) != null) {
      // If on HTTPS, create client with SSL Context
      SslContextFactory sslContextFactory = new SimpleContainerScope(
              WebSocketPolicy.newClientPolicy())
              .getSslContextFactory();
      webSocketClient = new WebSocketClient(sslContextFactory);
    } else {
      // local testing on HTTP
      webSocketClient = new WebSocketClient();
    }
    return webSocketClient;
  }

  private void sendMessageOverWebSocket(String message) throws Exception {
    if (!webSocketClient.isRunning()) {
      try {
        webSocketClient.start();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    ClientUpgradeRequest request = new ClientUpgradeRequest();
    // Attempt connection
    Future<Session> future = webSocketClient.connect(clientSocket,
        new URI(getWebSocketAddress()), request);
    // Wait for Connect
    Session session = future.get();
    // Send a message
    session.getRemote().sendString(message);
    // Close session
    session.close();
  }

  /**
   * Returns the host:port/echo address a client needs to use to communicate with the server.
   * On App engine Flex environments, result will be in the form wss://project-id.appspot.com/echo
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
