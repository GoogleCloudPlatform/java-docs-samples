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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

public class BuildIapRequest {
  private static final String IAM_SCOPE = "https://www.googleapis.com/auth/iam";
  private static final String OAUTH_TOKEN_URI = "https://www.googleapis.com/oauth2/v4/token";
  private static final String JWT_BEARER_TOKEN_GRANT_TYPE =
      "urn:ietf:params:oauth:grant-type:jwt-bearer";
  private static final long EXPIRATION_TIME_IN_SECONDS = 3600L;

  private static final HttpTransport httpTransport = new NetHttpTransport();

  private static Clock clock = Clock.systemUTC();

  private BuildIapRequest() {}

  private static IdTokenProvider getIdTokenProvider() throws Exception {
    GoogleCredentials credentials =
        GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IAM_SCOPE));
    // service account credentials are required to sign the jwt token
    if (credentials == null || !(credentials instanceof IdTokenProvider)) {
      throw new Exception("Google credentials : credentials that can provide id tokens expected");
    }
    return (IdTokenProvider) credentials;
  }

  /**
   * Clone request and add an IAP Bearer Authorization header with signed JWT token.
   *
   * @param request Request to add authorization header
   * @param iapClientId OAuth 2.0 client ID for IAP protected resource
   * @return Clone of request with Bearer style authorization header with signed jwt token.
   * @throws Exception exception creating signed JWT
   */
  public static HttpRequest buildIapRequest(HttpRequest request, String iapClientId)
      throws Exception {

    IdTokenProvider idTokenProvider = getIdTokenProvider();
    IdTokenCredentials credentials = IdTokenCredentials.newBuilder()
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
