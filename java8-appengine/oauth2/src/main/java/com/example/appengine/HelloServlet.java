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

package com.example.appengine;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {

  @Override
  public void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {

    resp.setContentType("text/plain");
    PrintWriter out = resp.getWriter();

    final String scope = "https://www.googleapis.com/auth/userinfo.email";
    OAuthService oauth = OAuthServiceFactory.getOAuthService();
    User user = null;
    try {
      user = oauth.getCurrentUser(scope);
    } catch (OAuthRequestException e) {
      getServletContext().log("Oauth error", e);
      out.print("auth error");
      return;
    }

    out.print("Hello world, welcome to Oauth2: " + user.getEmail());
  }
}
// [END example]
