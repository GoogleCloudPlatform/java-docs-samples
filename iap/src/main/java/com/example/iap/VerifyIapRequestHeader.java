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
import com.google.common.base.Preconditions;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URL;
import java.security.interfaces.ECPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Verify IAP authorization JWT token in incoming request. */
public class VerifyIapRequestHeader {

  private static final String PUBLIC_KEY_VERIFICATION_URL =
      "https://www.gstatic.com/iap/verify/public_key-jwk";

  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  // using a simple cache with no eviction for this sample
  private final Map<String, JWK> keyCache = new HashMap<>();

  private static Clock clock = Clock.systemUTC();

  private ECPublicKey getKey(String kid, String alg) throws Exception {
    JWK jwk = keyCache.get(kid);
    if (jwk == null) {
      // update cache loading jwk public key data from url
      JWKSet jwkSet = JWKSet.load(new URL(PUBLIC_KEY_VERIFICATION_URL));
      for (JWK key : jwkSet.getKeys()) {
        keyCache.put(key.getKeyID(), key);
      }
      jwk = keyCache.get(kid);
    }
    // confirm that algorithm matches
    if (jwk != null && jwk.getAlgorithm().getName().equals(alg)) {
      return ECKey.parse(jwk.toJSONString()).toECPublicKey();
    }
    return null;
  }

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

  private boolean verifyJwt(String jwtToken, String expectedAudience) throws Exception {

    // parse signed token into header / claims
    SignedJWT signedJwt = SignedJWT.parse(jwtToken);
    JWSHeader jwsHeader = signedJwt.getHeader();

    // header must have algorithm("alg") and "kid"
    Preconditions.checkNotNull(jwsHeader.getAlgorithm());
    Preconditions.checkNotNull(jwsHeader.getKeyID());

    JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

    // claims must have audience, issuer
    Preconditions.checkArgument(claims.getAudience().contains(expectedAudience));
    Preconditions.checkArgument(claims.getIssuer().equals(IAP_ISSUER_URL));

    // claim must have issued at time in the past
    Date currentTime = Date.from(Instant.now(clock));
    Preconditions.checkArgument(claims.getIssueTime().before(currentTime));
    // claim must have expiration time in the future
    Preconditions.checkArgument(claims.getExpirationTime().after(currentTime));

    // must have subject, email
    Preconditions.checkNotNull(claims.getSubject());
    Preconditions.checkNotNull(claims.getClaim("email"));

    // verify using public key : lookup with key id, algorithm name provided
    ECPublicKey publicKey = getKey(jwsHeader.getKeyID(), jwsHeader.getAlgorithm().getName());

    Preconditions.checkNotNull(publicKey);
    JWSVerifier jwsVerifier = new ECDSAVerifier(publicKey);
    return signedJwt.verify(jwsVerifier);
  }
}
// [END iap_validate_jwt]
