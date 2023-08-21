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

import static com.google.cdn.SignedCookies.signCookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SignedCookiesTest {

  private static long EXPIRATION = 1518135754;
  private static byte[] KEY_BYTES = Base64.getUrlDecoder().decode("aaaaaaaaaaaaaaaaaaaaaa==");
  private static String KEY_NAME = "my-key";
  private static String URL_PREFIX = "https://media.example.com/videos/";

  private static String INVALID_URL_PREFIX_1 = "www.media.example.com/videos/";
  private static String INVALID_URL_PREFIX_2 = "https://media.example.com/videos/?foo";

  @Test
  public void testUrlPathSignedWithPrefix() throws Exception {
    String result = signCookie(URL_PREFIX, KEY_BYTES, KEY_NAME, EXPIRATION);
    final String expected = "Cloud-CDN-Cookie="
        + "URLPrefix=aHR0cHM6Ly9tZWRpYS5leGFtcGxlLmNvbS92aWRlb3Mv"
        + ":Expires=1518135754:KeyName=my-key"
        + ":Signature=c2oZduDcTH36_bCbO-hEoaLc_5o=";
    assertEquals(expected, result);
  }

  @Test
  public void testUrlPathSignedWithPrefixInvalidPrefix() throws Exception {
    assertThrows(IllegalArgumentException.class,
        () -> {
          signCookie(INVALID_URL_PREFIX_1, KEY_BYTES, KEY_NAME, EXPIRATION);
        });
    assertThrows(IllegalArgumentException.class,
        () -> {
          signCookie(INVALID_URL_PREFIX_2, KEY_BYTES, KEY_NAME, EXPIRATION);
        });
  }
}
