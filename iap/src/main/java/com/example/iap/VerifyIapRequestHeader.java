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
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.PemReader.Section;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
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

  private final Map<String, ECPublicKey> keyCache = new HashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();
  private final TypeReference<HashMap<String, String>> typeRef =
      new TypeReference<HashMap<String, String>>() {};

  private ECDSAKeyProvider keyProvider =
      new ECDSAKeyProvider() {
        @Override
        public ECPublicKey getPublicKeyById(String kid) {
          ECPublicKey key = keyCache.get(kid);
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
              if (!keyData.getKey().equals(kid)) {
                continue;
              }
              key = getKey(keyData.getValue());
              if (key != null) {
                keyCache.putIfAbsent(kid, key);
              }
            }

          } catch (IOException e) {
            // ignore exception
          }

          return key;
        }

        @Override
        public ECPrivateKey getPrivateKey() {
          // ignore : only required for signing requests
          return null;
        }

        @Override
        public String getPrivateKeyId() {
          // ignore : only required for signing requests
          return null;
        }
      };

  private static String getBaseUrl(URL url) throws Exception {
    String urlFilePath = url.getFile();
    int pathDelim = urlFilePath.lastIndexOf('/');
    String path = (pathDelim > 0) ? urlFilePath.substring(0, pathDelim) : "";
    return (url.getProtocol() + "://" + url.getHost() + path).trim();
  }

  DecodedJWT verifyJWTToken(HttpRequest request) throws Exception {
    // Check for iap jwt header in incoming request
    String jwtToken =
        request.getHeaders().getFirstHeaderStringValue("x-goog-authenticated-user-jwt");
    if (jwtToken == null) {
      return null;
    }
    String baseUrl = getBaseUrl(request.getUrl().toURL());
    return verifyJWTToken(jwtToken, baseUrl);
  }

  DecodedJWT verifyJWTToken(String jwtToken, String baseUrl) throws Exception {
    Algorithm algorithm = Algorithm.ECDSA256(keyProvider);

    // Time constraints are automatically checked, use acceptLeeway to specify a leeway window
    // The token was issued in a past date "iat" < TODAY
    // The token hasn't expired yet "exp" > TODAY
    JWTVerifier verifier =
        JWT.require(algorithm).withAudience(baseUrl).withIssuer(IAP_ISSUER_URL).build();

    DecodedJWT decodedJWT = verifier.verify(jwtToken);

    if (decodedJWT.getSubject() == null) {
      throw new JWTVerificationException("Subject expected, not found");
    }
    if (decodedJWT.getClaim("email") == null) {
      throw new JWTVerificationException("Email expected, not found");
    }
    return decodedJWT;
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
