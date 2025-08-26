/* Copyright 2016 Google LLC
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

// [START gae_java21_users_api]

package com.example.appengine.users;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "UserAPI",
    description = "UserAPI: Login / Logout with UserService",
    urlPatterns = "/userapi"
)
public class UsersServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String thisUrl = req.getRequestURI();

    resp.setContentType("text/html");
    if (req.getUserPrincipal() != null) {
      resp.getWriter()
          .println(
              "<p>Hello, "
                  + req.getUserPrincipal().getName()
                  + "!  You can <a href=\""
                  + userService.createLogoutURL(thisUrl)
                  + "\">sign out</a>.</p>");
    } else {
      resp.getWriter()
          .println(
              "<p>Please <a href=\"" + userService.createLoginURL(thisUrl) + "\">sign in</a>.</p>");
    }
  }
}
// [END gae_java21_users_api]
