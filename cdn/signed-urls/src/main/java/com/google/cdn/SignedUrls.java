/*
 * Copyright 2018 Google Inc.
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Samples to create a signed URL for a Cloud CDN endpoint
 */
public class SignedUrls {

  // [START cloudcdn_sign_url]
  /**
   * Creates a signed URL for a Cloud CDN endpoint with the given key
   * URL must start with http:// or https://, and must contain a forward
   * slash (/) after the hostname.
   *
   * @param url the Cloud CDN endpoint to sign
   * @param key url signing key uploaded to the backend service/bucket, as a 16-byte array
   * @param keyName the name of the signing key added to the back end bucket or service
   * @param expirationTime the date that the signed URL expires
   * @return a properly formatted signed URL
   * @throws InvalidKeyException when there is an error generating the signature for the input key
   * @throws NoSuchAlgorithmException when HmacSHA1 algorithm is not available in the environment
   */
  public static String signUrl(String url,
                               byte[] key,
                               String keyName,
                               Date expirationTime)
          throws InvalidKeyException, NoSuchAlgorithmException {

    final long unixTime = expirationTime.getTime() / 1000;

    String urlToSign = url
                        + (url.contains("?") ? "&" : "?")
                        + "Expires=" + unixTime
                        + "&KeyName=" + keyName;

    String encoded = SignedUrls.getSignature(key, urlToSign);
    return urlToSign + "&Signature=" + encoded;
  }

  public static String getSignature(byte[] privateKey, String input)
      throws InvalidKeyException, NoSuchAlgorithmException {

    final String algorithm = "HmacSHA1";
    final int offset = 0;
    Key key = new SecretKeySpec(privateKey, offset, privateKey.length, algorithm);
    Mac mac = Mac.getInstance(algorithm);
    mac.init(key);
    return  Base64.getUrlEncoder().encodeToString(mac.doFinal(input.getBytes()));
  }
  // [END cloudcdn_sign_url]

  // [START cloudcdn_sign_url_prefix]
  /**
   * Creates a signed URL with a URL prefix for a Cloud CDN endpoint with the given key. Prefixes
   * allow access to any URL with the same prefix, and can be useful for granting access broader
   * content without signing multiple URLs.
   *
   * @param requestUrl URL of request
   * @param urlPrefix URL prefix to sign as a string. urlPrefix must start with either http:// or
   * https:// and should not include query parameters
   * @param key url signing key uploaded to the backend service/bucket, as a 16-byte array
   * @param keyName the name of the signing key added to the back end bucket or service
   * @param expirationTime the date that the signed URL expires
   * @return a properly formatted signed URL with URL prefix
   * @throws InvalidKeyException when there is an error generating the signature for the input key
   * @throws NoSuchAlgorithmException when HmacSHA1 algorithm is not available in the environment
   * @throws IllegalArgumentException when urlPrefix string is malformed.
   */
  static String signUrlWithPrefix(String requestUrl,
      String urlPrefix,
      byte[] key,
      String keyName,
      Date expirationTime)
      throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException {

    if (urlPrefix.contains("?") || urlPrefix.contains("#")) {
      throw new IllegalArgumentException("urlPrefix must not include query params: " + urlPrefix);
    }
    if (!urlPrefix.startsWith("http://") && !urlPrefix.startsWith("https://")) {
      throw new IllegalArgumentException(
          "urlPrefix must start with either http:// or https://: " + urlPrefix);
    }
    final long unixTime = expirationTime.getTime() / 1000;

    String encodedUrlPrefix = Base64.getUrlEncoder().encodeToString(urlPrefix.getBytes(
        StandardCharsets.UTF_8));
    String urlToSign = "URLPrefix=" + encodedUrlPrefix
        + "&Expires=" + unixTime
        + "&KeyName=" + keyName;

    String encoded = SignedUrls.getSignatureForUrl(key, urlToSign);
    return requestUrl + "&" + urlToSign + "&Signature=" + encoded;
  }

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
  // [END cloudcdn_sign_url_prefix]

  public static void main(String[] args) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 1);
    Date tomorrow = cal.getTime();

    //read the key as a base 64 url-safe encoded string, then convert to byte array
    final String keyPath = "/path/to/key";
    String base64String = new String(Files.readAllBytes(Paths.get(keyPath)),
        StandardCharsets.UTF_8);
    byte[] keyBytes = Base64.getUrlDecoder().decode(base64String);

    String signUrlResult = signUrl("https://example.com/foo?", keyBytes, "YOUR-KEY-NAME", tomorrow);
    System.out.println(signUrlResult);

    String signUrlWithPrefixResult = signUrlWithPrefix(
        "https://media.example.com/videos/id/master.m3u8?userID=abc123&starting_profile=1",
        "https://media.example.com/videos/", keyBytes, "YOUR-KEY-NAME", tomorrow);
    System.out.println(signUrlWithPrefixResult);
  }
}
