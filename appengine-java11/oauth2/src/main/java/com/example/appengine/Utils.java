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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class Utils {

  /** Get application name from the runtime environment variable */
  static final String APP_NAME = System.getenv("GAE_APPLICATION");

  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static final MemoryDataStoreFactory DATA_STORE_FACTORY =
      MemoryDataStoreFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  static final GsonFactory JSON_FACTORY = new GsonFactory();

  /** Set your OAuth 2.0 Client Credentials */
  private static String CLIENT_ID = System.getenv("CLIENT_ID");

  private static String CLIENT_SECRET = System.getenv("CLIENT_SECRET");

  /** Scopes for requesting access to Google OAuth2 API */
  private static final List<String> SCOPES =
      Arrays.asList(
          "https://www.googleapis.com/auth/userinfo.profile",
          "https://www.googleapis.com/auth/userinfo.email");

  /** Returns the redirect URI for the given HTTP servlet request. */
  static String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    url.setScheme("https");
    return url.build();
  }

  // [START gae_java11_oauth2_code_flow]
  /**
   * Loads the authorization code flow to be used across all HTTP servlet requests. It is only
   * called during the first HTTP servlet request.
   */
  public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPES)
        .setDataStoreFactory(DATA_STORE_FACTORY)
        .build();
  }
  // [END gae_java11_oauth2_code_flow]

  /**
   * Returns the user ID for the given HTTP servlet request. This identifies your application's user
   * and is used to assign and persist credentials to that user. Most commonly, this will be a user
   * id stored in the session or even the session id itself.
   */
  static String getUserId(HttpServletRequest req) throws ServletException, IOException {
    return req.getSession().getId();
  }

  // [START gae_java11_oauth2_get_user]
  /** Obtain end-user authorization grant for Google APIs and return username */
  public static String getUserInfo(Credential credential) throws IOException {
    Oauth2 oauth2Client =
        new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APP_NAME)
            .build();

    // Retrieve user profile
    Userinfo userInfo = oauth2Client.userinfo().get().execute();
    String username = userInfo.getGivenName();
    return username;
  }
  // [END gae_java11_oauth2_get_user]
}
