/*
 * Copyright 2018 Google LLC
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

package com.example.app;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;



/**
 * JWTClient shows how a client can authenticate with a Cloud Endpoints service
 */
public class GoogleJwtClient {

  // [START endpoints_generate_jwt_sa]
  /**
   * Generates a signed JSON Web Token using a Google API Service Account
   * utilizes com.auth0.jwt.
   */
  public static String generateJwt(final String saKeyfile, final String saEmail,
      final String audience, final int expiryLength)
      throws FileNotFoundException, IOException {

    Date now = new Date();
    Date expTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiryLength));

    // Build the JWT payload
    JWTCreator.Builder token = JWT.create()
        .withIssuedAt(now)
        // Expires after 'expiraryLength' seconds
        .withExpiresAt(expTime)
        // Must match 'issuer' in the security configuration in your
        // swagger spec (e.g. service account email)
        .withIssuer(saEmail)
        // Must be either your Endpoints service name, or match the value
        // specified as the 'x-google-audience' in the OpenAPI document
        .withAudience(audience)
        // Subject and email should match the service account's email
        .withSubject(saEmail)
        .withClaim("email", saEmail);

    // Sign the JWT with a service account
    FileInputStream stream = new FileInputStream(saKeyfile);
    GoogleCredential cred = GoogleCredential.fromStream(stream);
    RSAPrivateKey key = (RSAPrivateKey) cred.getServiceAccountPrivateKey();
    Algorithm algorithm = Algorithm.RSA256(null, key);
    return token.sign(algorithm);
  }
  // [END endpoints_generate_jwt_sa]


  // [START endpoints_jwt_request]
  /**
   * Makes an authorized request to the endpoint.
   */
  public static String makeJwtRequest(final String singedJwt, final URL url)
      throws IOException, ProtocolException {

    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Authorization", "Bearer " + singedJwt);

    InputStreamReader reader = new InputStreamReader(con.getInputStream());
    BufferedReader buffReader = new BufferedReader(reader);

    String line;
    StringBuilder result = new StringBuilder();
    while ((line = buffReader.readLine()) != null) {
      result.append(line);
    }
    buffReader.close();
    return result.toString();
  }
  // [END endpoints_jwt_request]
}
