/*
 * Copyright 2017 Google Inc.
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

package com.example.iap;
// [START iap_make_request]

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.Collections;

public class BuildIapRequest {
  private static final String IAM_SCOPE = "https://www.googleapis.com/auth/iam";

  private static final HttpTransport httpTransport = new NetHttpTransport();

  private BuildIapRequest() {}

  private static IdTokenProvider getIdTokenProvider() throws IOException {
    GoogleCredentials credentials =
        GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IAM_SCOPE));

    Preconditions.checkNotNull(credentials, "Expected to load credentials");
    Preconditions.checkState(
        credentials instanceof IdTokenProvider,
        String.format(
            "Expected credentials that can provide id tokens, got %s instead",
            credentials.getClass().getName()));

    return (IdTokenProvider) credentials;
  }

  /**
   * Clone request and add an IAP Bearer Authorization header with signed JWT token.
   *
   * @param request Request to add authorization header
   * @param iapClientId OAuth 2.0 client ID for IAP protected resource
   * @return Clone of request with Bearer style authorization header with signed jwt token.
   * @throws IOException exception creating signed JWT
   */
  public static HttpRequest buildIapRequest(HttpRequest request, String iapClientId)
      throws IOException {

    IdTokenProvider idTokenProvider = getIdTokenProvider();
    IdTokenCredentials credentials =
        IdTokenCredentials.newBuilder()
            .setIdTokenProvider(idTokenProvider)
            .setTargetAudience(iapClientId)
            .build();

    HttpRequestInitializer httpRequestInitializer = new HttpCredentialsAdapter(credentials);

    return httpTransport
        .createRequestFactory(httpRequestInitializer)
        .buildRequest(request.getRequestMethod(), request.getUrl(), request.getContent());
  }
}
// [END iap_make_request]
