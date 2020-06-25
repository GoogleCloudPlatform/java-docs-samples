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
// [START iap_validate_jwt]

import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.TokenVerifier;

/** Verify IAP authorization JWT token in incoming request. */
public class VerifyIapRequestHeader {

  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  // Verify jwt tokens addressed to IAP protected resources on App Engine.
  // The project *number* for your Google Cloud project via 'gcloud projects describe $PROJECT_ID'
  // The project *number* can also be retrieved from the Project Info card in Cloud Console.
  // projectId is The project *ID* for your Google Cloud Project.
  boolean verifyJwtForAppEngine(HttpRequest request, long projectNumber, String projectId)
      throws Exception {
    // Check for iap jwt header in incoming request
    String jwt = request.getHeaders().getFirstHeaderStringValue("x-goog-iap-jwt-assertion");
    if (jwt == null) {
      return false;
    }
    return verifyJwt(
        jwt,
        String.format("/projects/%s/apps/%s", Long.toUnsignedString(projectNumber), projectId));
  }

  boolean verifyJwtForComputeEngine(
      HttpRequest request, long projectNumber, long backendServiceId) throws Exception {
    // Check for iap jwt header in incoming request
    String jwtToken = request.getHeaders()
        .getFirstHeaderStringValue("x-goog-iap-jwt-assertion");
    if (jwtToken == null) {
      return false;
    }
    return verifyJwt(
        jwtToken,
        String.format(
            "/projects/%s/global/backendServices/%s",
            Long.toUnsignedString(projectNumber), Long.toUnsignedString(backendServiceId)));
  }

  private boolean verifyJwt(String jwtToken, String expectedAudience) {
    TokenVerifier tokenVerifier = TokenVerifier.newBuilder()
        .setAudience(expectedAudience)
        .setIssuer(IAP_ISSUER_URL)
        .build();
    try {
      JsonWebToken jsonWebToken = tokenVerifier.verify(jwtToken);

      // Verify that the token contain subject and email claims
      JsonWebToken.Payload payload = jsonWebToken.getPayload();
      return payload.getSubject() != null && payload.get("email") != null;
    } catch (TokenVerifier.VerificationException e) {
      System.out.println(e.getMessage());
      return false;
    }
  }
}
// [END iap_validate_jwt]
