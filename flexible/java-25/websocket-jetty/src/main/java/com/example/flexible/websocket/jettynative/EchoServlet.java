/*
 * Copyright 2024 Google LLC
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

import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/*
 * Server-side WebSocket upgraded on /echo servlet.
 */
@SuppressWarnings("serial")
@WebServlet(
    name = "Echo WebSocket Servlet",
    urlPatterns = {"/echo"})
public class EchoServlet extends WebSocketServlet implements WebSocketCreator {
  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.setCreator(this);
  }

  @Override
  public Object createWebSocket(
      ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
    return new ServerSocket();
  }
}
