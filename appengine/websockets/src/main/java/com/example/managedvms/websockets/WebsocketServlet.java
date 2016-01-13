/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.websockets;

import com.google.common.io.CharStreams;

import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.DeploymentException;

// [START example]
@SuppressWarnings("serial")
public class WebsocketServlet extends HttpServlet {

  private static String METADATA_NETWORK_INTERFACE_URL =
      "http://metadata/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip";
  private static String EXTERNAL_IP = "localhost";

  @Override
  public void init() throws ServletException {
    EXTERNAL_IP = getExternalIp();
    Server server = new Server(EXTERNAL_IP, 65080, "/", null, EchoEndpoint.class);
    try {
      server.start();
    } catch (DeploymentException e) {
      throw new ServletException("Deployment error", e);
    }
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    req.getSession().getServletContext().setAttribute("external_ip", EXTERNAL_IP);
    req.getRequestDispatcher("/index.jsp").forward(req, resp);
  }

  public static String getExternalIp() {
    try {
      URL url = new URL(METADATA_NETWORK_INTERFACE_URL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Metadata-Flavor", "Google");
      try (InputStreamReader in = new InputStreamReader(con.getInputStream(), "UTF-8")) {
        return CharStreams.toString(in);
      }
    } catch (IOException e) {
      return "localhost";
    }
  }
}
// [END example]
