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

  // [START signUrl]
  /**
   * Creates a signed URL for a Cloud CDN endpoint with the given key
   * should pass in a properly formatted URL
   * will work:
   *   https://www.google.com/
   *   http://www.google.com/
   *   https://www.google.com/?foo=test&bar=test
   *   https://www.google.com/foo
   *
   * won't work:
   *    https://www.google.com
   *    https://www.google.com?test
   *    www.google.com
   *
   * @param url the Cloud CDN endpoint to sign
   * @param key encoded as a 16-byte array
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
    Key key = new SecretKeySpec(privateKey, 0, privateKey.length, algorithm);
    Mac mac = Mac.getInstance(algorithm);
    mac.init(key);
    return  Base64.getUrlEncoder().encodeToString(mac.doFinal(input.getBytes()));
  }
  // [END signUrl]

  public static void main(String[] args) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 1);
    Date tomorrow = cal.getTime();

    //read the key as a base 64 url-safe encoded string
    final String keyPath = "./src/main/resources/my-key";
    String base64String = new String(Files.readAllBytes(Paths.get(keyPath)));
    //turn the key string into a byte array
    byte[] keyBytes = Base64.getUrlDecoder().decode(base64String);

    String result = signUrl("http://35.186.234.33/index.html", keyBytes, "my-key", tomorrow);
    System.out.println(result);
  }
}
