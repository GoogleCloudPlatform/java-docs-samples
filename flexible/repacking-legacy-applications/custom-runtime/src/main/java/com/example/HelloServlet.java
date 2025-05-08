/*
 * Copyright 2025 Google LLC
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

package com.example;

// [START gae_hello_world_servlet]

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public final class HelloServlet extends HttpServlet {
  /**
   * This method handles GET requests to the /hello endpoint.
   *
   * <p>Subclasses should not override this method.
   *
   * @param request the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(
      final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    response.getWriter().println("<h1>Hello, World!</h1>");
  }
}
// [END gae_hello_world_servlet]
