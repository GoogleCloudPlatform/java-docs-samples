/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appengine;

import static com.google.appengine.api.utils.SystemProperty.environment;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.oauth.OAuthServiceFailureException;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *  Filter to verify that request has a "Authorization: Bearer xxxx" header,
 *  and check if xxxx is authorized to use this app.
 *
 *  <p>Note - this is to demonstrate the OAuth2 APIs, as it is possible to lockdown some
 *  of your app's URL's using cloud console by adding service accounts to the project.</p>
 */
public class Oauth2Filter implements Filter {

  private ServletContext context;

  @Override
  public void init(final FilterConfig config) throws ServletException {
    this.context = config.getServletContext();
  }

  // [START oauth2]
  @Override
  public void doFilter(
      final ServletRequest servletReq, final ServletResponse servletResp,  final FilterChain chain)
      throws IOException, ServletException {
    final String scope = "https://www.googleapis.com/auth/userinfo.email";
    ImmutableSet<String> allowedClients = new ImmutableSet.Builder<String>()
        .add("407408718192.apps.googleusercontent.com")
        .add("755878275993-j4k7emq6rlupctce1c28enpcrr50vfo1.apps.googleusercontent.com")
        .build();

    HttpServletResponse resp = (HttpServletResponse) servletResp;

    OAuthService oauth = OAuthServiceFactory.getOAuthService();

    // Only check Oauth2 when in production, skip if run in development.
    SystemProperty.Environment.Value env = environment.value();
    if (env == SystemProperty.Environment.Value.Production) { // APIs only work in Production
      try {
        String tokenAudience = oauth.getClientId(scope);

          // The line below is commented out for privacy.
//        context.log("tokenAudience: " + tokenAudience);   // Account we match

        if (!allowedClients.contains(tokenAudience)) {
          throw new OAuthRequestException("audience of token '" + tokenAudience
              + "' is not in allowed list " + allowedClients);
        }
      } catch (OAuthRequestException ex) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);    // Not allowed
        return;
      } catch (OAuthServiceFailureException ex) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);    // some failure - reject
        context.log("oauth2 failure", ex);
        return;
      }
    }
    chain.doFilter(servletReq, servletResp);  // continue processing
  }
  // [END oauth2]

  @Override
  public void destroy() { }

}
