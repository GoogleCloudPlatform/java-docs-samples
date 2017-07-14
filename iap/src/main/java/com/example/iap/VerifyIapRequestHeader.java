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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.PemReader.Section;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.impl.DefaultClaims;

import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/** Verify IAP authorization JWT token in incoming request. */
public class VerifyIapRequestHeader {

  // [START verify_iap_request]
  private static final String PUBLIC_KEY_VERIFICATION_URL =
      "https://www.gstatic.com/iap/verify/public_key";
  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  private final Map<String, Key> keyCache = new HashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();
  private final TypeReference<HashMap<String, String>> typeRef =
      new TypeReference<HashMap<String, String>>() {};

  private SigningKeyResolver resolver =
      new SigningKeyResolver() {
        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {
          return resolveSigningKey(header);
        }

        @Override
        public Key resolveSigningKey(JwsHeader header, String payload) {
          return resolveSigningKey(header);
        }

        private Key resolveSigningKey(JwsHeader header) {
          String keyId = header.getKeyId();
          Key key = keyCache.get(keyId);
          if (key != null) {
            return key;
          }
          try {
            HttpRequest request =
                new NetHttpTransport()
                    .createRequestFactory()
                    .buildGetRequest(new GenericUrl(PUBLIC_KEY_VERIFICATION_URL));
            HttpResponse response = request.execute();
            if (response.getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
              return null;
            }
            Map<String, String> keys = mapper.readValue(response.parseAsString(), typeRef);
            for (Map.Entry<String, String> keyData : keys.entrySet()) {
              if (!keyData.getKey().equals(keyId)) {
                continue;
              }
              key = getKey(keyData.getValue());
              if (key != null) {
                keyCache.putIfAbsent(keyId, key);
              }
            }

          } catch (IOException e) {
            // ignore exception
          }
          return key;
        }
      };

  // Verify jwt tokens addressed to IAP protected resources on App Engine.
  // The project *number* for your Google Cloud project available via 'gcloud projects describe $PROJECT_ID'
  // or in the Project Info card in Cloud Console.
  // projectId is The project *ID* for your Google Cloud Project.
  Jwt verifyJWTTokenForAppEngine(HttpRequest request, long projectNumber, String projectId) throws Exception {
    // Check for iap jwt header in incoming request
    String jwtToken =
        request.getHeaders().getFirstHeaderStringValue("x-goog-iap-jwt-assertion");
    if (jwtToken == null) {
      return null;
    }
    return verifyJWTToken(jwtToken, String.format("/projects/%s/apps/%s",
                                                  Long.toUnsignedString(projectNumber),
                                                 projectId));
  }

  Jwt verifyJWTTokenForComputeEngine(HttpRequest request, long projectNumber, long backendServiceId) throws Exception {
    // Check for iap jwt header in incoming request
    String jwtToken =
        request.getHeaders().getFirstHeaderStringValue("x-goog-iap-jwt-assertion");
    if (jwtToken == null) {
      return null;
    }
    return verifyJWTToken(jwtToken, String.format("/projects/%s/global/backendServices/%s",
                                                  Long.toUnsignedString(projectNumber),
                                                  Long.toUnsignedString(backendServiceId)));
  }
  
  Jwt verifyJWTToken(String jwtToken, String expectedAudience) throws Exception {
    // Time constraints are automatically checked, use setAllowedClockSkewSeconds
    // to specify a leeway window
    // The token was issued in a past date "iat" < TODAY
    // The token hasn't expired yet "exp" > TODAY
    Jwt jwt =
        Jwts.parser()
            .setSigningKeyResolver(resolver)
            .requireAudience(expectedAudience)
            .requireIssuer(IAP_ISSUER_URL)
            .parse(jwtToken);
    DefaultClaims claims = (DefaultClaims) jwt.getBody();
    if (claims.getSubject() == null) {
      throw new Exception("Subject expected, not found.");
    }
    if (claims.get("email") == null) {
      throw new Exception("Email expected, not found.");
    }
    return jwt;
  }

  private ECPublicKey getKey(String keyText) throws IOException {
    StringReader reader = new StringReader(keyText);
    Section section = PemReader.readFirstSectionAndClose(reader, "PUBLIC KEY");
    if (section == null) {
      throw new IOException("Invalid data.");
    } else {
      byte[] bytes = section.getBase64DecodedBytes();
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
      try {
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey publicKey = kf.generatePublic(keySpec);
        if (publicKey instanceof ECPublicKey) {
          return (ECPublicKey) publicKey;
        }
      } catch (InvalidKeySpecException | NoSuchAlgorithmException var7) {
        throw new IOException("Unexpected exception reading data", var7);
      }
    }
    return null;
  }
  // [END verify_iap_request]
}
