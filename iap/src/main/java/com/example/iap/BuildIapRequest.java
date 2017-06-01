/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.iap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

public class BuildIapRequest {
  // [START generate_iap_request]
  private static final String IAM_SCOPE = "https://www.googleapis.com/auth/iam";
  private static final String OAUTH_TOKEN_URI = "https://www.googleapis.com/oauth2/v4/token";
  private static final String JWT_BEARER_TOKEN_GRANT_TYPE =
      "urn:ietf:params:oauth:grant-type:jwt-bearer";
  private static final long EXPIRATION_TIME_IN_SECONDS = 3600L;

  private static final HttpTransport httpTransport = new NetHttpTransport();

  private static Clock clock = Clock.systemUTC();

  private BuildIapRequest() {}

  private static String getBaseUrl(URL url) throws Exception {
    String urlFilePath = url.getFile();
    int pathDelim = urlFilePath.lastIndexOf('/');
    String path = (pathDelim > 0) ? urlFilePath.substring(0, pathDelim) : "";
    return (url.getProtocol() + "://" + url.getHost() + path).trim();
  }

  private static ServiceAccountCredentials getCredentials() throws Exception {
    GoogleCredentials credentials =
        GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IAM_SCOPE));
    // service account credentials are required to sign the jwt token
    if (credentials == null || !(credentials instanceof ServiceAccountCredentials)) {
      throw new Exception("Google credentials : service accounts credentials expected");
    }
    return (ServiceAccountCredentials) credentials;
  }

  private static String getSignedJWToken(ServiceAccountCredentials credentials, String baseUrl)
      throws IOException {
    Instant now = Instant.now(clock);
    long expirationTime = now.getEpochSecond() + EXPIRATION_TIME_IN_SECONDS;
    // generate jwt signed by service account
    return JWT.create()
        .withKeyId(credentials.getPrivateKeyId())
        .withAudience(OAUTH_TOKEN_URI)
        .withIssuer(credentials.getClientEmail())
        .withSubject(credentials.getClientEmail())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(Instant.ofEpochSecond(expirationTime)))
        .withClaim("target_audience", baseUrl)
        .sign(Algorithm.RSA256(null, (RSAPrivateKey) credentials.getPrivateKey()));
  }

  private static String getGoogleIdToken(String jwt) throws Exception {
    final GenericData tokenRequest =
        new GenericData().set("grant_type", JWT_BEARER_TOKEN_GRANT_TYPE).set("assertion", jwt);
    final UrlEncodedContent content = new UrlEncodedContent(tokenRequest);

    final HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

    final HttpRequest request =
        requestFactory
            .buildPostRequest(new GenericUrl(OAUTH_TOKEN_URI), content)
            .setParser(new JsonObjectParser(JacksonFactory.getDefaultInstance()));

    HttpResponse response;
    String idToken = null;
    response = request.execute();
    GenericData responseData = response.parseAs(GenericData.class);
    idToken = (String) responseData.get("id_token");
    return idToken;
  }

  public static HttpRequest buildIAPRequest(HttpRequest request) throws Exception {
    // get service account credentials
    ServiceAccountCredentials credentials = getCredentials();
    // get the base url of the request URL
    String baseUrl = getBaseUrl(request.getUrl().toURL());
    String jwt = getSignedJWToken(credentials, baseUrl);
    if (jwt == null) {
      throw new Exception(
          "Unable to create a signed jwt token for : "
              + baseUrl
              + "with issuer : "
              + credentials.getClientEmail());
    }

    String idToken = getGoogleIdToken(jwt);
    if (idToken == null) {
      throw new Exception("Unable to retrieve open id token");
    }

    // Create an authorization header with bearer token
    HttpHeaders httpHeaders = request.getHeaders().clone().setAuthorization("Bearer " + idToken);

    // create request with jwt authorization header
    return httpTransport
        .createRequestFactory()
        .buildRequest(request.getRequestMethod(), request.getUrl(), request.getContent())
        .setHeaders(httpHeaders);
  }
  // [END generate_iap_request]
}
