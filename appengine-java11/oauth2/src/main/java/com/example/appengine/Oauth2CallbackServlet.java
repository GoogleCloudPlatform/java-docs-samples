/*
 * Copyright 2019 Google LLC
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

// [START gae_java11_oauth2_callback]
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet class extends AbstractAuthorizationCodeServlet which if the end-user credentials are
 * not found, will redirect the end-user to an authorization page.
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/oauth2callback/*")
public class Oauth2CallbackServlet extends AbstractAuthorizationCodeCallbackServlet {

  /** Handles a successfully granted authorization. */
  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  /** Handles an error to the authorization, such as when an end user denies authorization. */
  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    resp.getWriter().print("<p>You Denied Authorization.</p>");
    resp.setStatus(200);
    resp.addHeader("Content-Type", "text/html");
  }

  /** Returns the redirect URI for the given HTTP servlet request. */
  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  /**
   * Loads the authorization code flow to be used across all HTTP servlet requests (only called
   * during the first HTTP servlet request with an authorization code).
   */
  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }

  /**
   * Returns the user ID for the given HTTP servlet request. This identifies your application's user
   * and is used to assign and persist credentials to that user.
   */
  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getUserId(req);
  }
}
// [END gae_java11_oauth2_callback]
