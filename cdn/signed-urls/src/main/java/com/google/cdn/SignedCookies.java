/*
 * Copyright 2023 Google LLC
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

package com.google.cdn;

// [START cloudcdn_sign_cookie]
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignedCookies {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.

    // The name of the signing key must match a key added to the back end bucket or service.
    String keyName = "YOUR-KEY-NAME";
    // Path to the URL signing key uploaded to the backend service/bucket.
    String keyPath = "/path/to/key";
    // The Unix timestamp that the signed URL expires.
    long expirationTime = ZonedDateTime.now().plusDays(1).toEpochSecond();
    // URL prefix to sign as a string. URL prefix must start with either "http://" or "https://"
    // and must not include query parameters.
    String urlPrefix = "https://media.example.com/videos/";

    // Read the key as a base64 url-safe encoded string, then convert to byte array.
    // Key used in signing must be in raw form (not base64url-encoded).
    String base64String = new String(Files.readAllBytes(Paths.get(keyPath)),
        StandardCharsets.UTF_8);
    byte[] keyBytes = Base64.getUrlDecoder().decode(base64String);

    // Create signed cookie from policy.
    String signedCookie = signCookie(urlPrefix, keyBytes, keyName, expirationTime);
    System.out.println(signedCookie);
  }

  // Creates a signed cookie for the specified policy.
  public static String signCookie(String urlPrefix, byte[] key, String keyName,
      long expirationTime)
      throws InvalidKeyException, NoSuchAlgorithmException {

    // Validate input URL prefix.
    try {
      URL validatedUrlPrefix = new URL(urlPrefix);
      if (!validatedUrlPrefix.getProtocol().startsWith("http")) {
        throw new IllegalArgumentException(
            "urlPrefix must start with either http:// or https://: " + urlPrefix);
      }
      if (validatedUrlPrefix.getQuery() != null) {
        throw new IllegalArgumentException("urlPrefix must not include query params: " + urlPrefix);
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          "urlPrefix malformed: " + urlPrefix);
    }

    String encodedUrlPrefix = Base64.getUrlEncoder().encodeToString(urlPrefix.getBytes(
        StandardCharsets.UTF_8));
    String policyToSign = String.format("URLPrefix=%s:Expires=%d:KeyName=%s", encodedUrlPrefix,
        expirationTime, keyName);

    String signature = getSignatureForUrl(key, policyToSign);
    return String.format("Cloud-CDN-Cookie=%s:Signature=%s", policyToSign, signature);
  }

  // Creates signature for input string with private key.
  private static String getSignatureForUrl(byte[] privateKey, String input)
      throws InvalidKeyException, NoSuchAlgorithmException {

    final String algorithm = "HmacSHA1";
    final int offset = 0;
    Key key = new SecretKeySpec(privateKey, offset, privateKey.length, algorithm);
    Mac mac = Mac.getInstance(algorithm);
    mac.init(key);
    return Base64.getUrlEncoder()
        .encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
  }
}
// [END cloudcdn_sign_cookie]
