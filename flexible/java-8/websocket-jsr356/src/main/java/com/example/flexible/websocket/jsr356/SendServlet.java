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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

@WebServlet("/send")
/** Servlet that converts the message sent over POST to be over websocket. */
public class SendServlet extends HttpServlet {

  private Logger logger = Logger.getLogger(SendServlet.class.getName());
  private final String webSocketAddress = ServerSocket.getWebSocketAddress();
  private ClientSocket clientSocket;

  private void initializeWebSocket() throws Exception {
    clientSocket = new ClientSocket(new URI(webSocketAddress));
    clientSocket.waitOnOpen();
    logger.info("REST service: open websocket client at " + webSocketAddress);
  }

  private void sendMessageOverWebSocket(String message) throws Exception {
    if (clientSocket == null) {
      try {
        initializeWebSocket();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    clientSocket.sendMessage(message);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String message = request.getParameter("message");
    Preconditions.checkNotNull(message);
    try {
      sendMessageOverWebSocket(message);
      response.sendRedirect("/");
    } catch (Exception e) {
      e.printStackTrace(response.getWriter());
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
  }
}
