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

import static com.google.cdn.SignedUrls.signUrl;
import static junit.framework.TestCase.assertEquals;

import java.util.Base64;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test SignedUrls samples
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SignedUrlsTest {
  private static long TIMESTAMP = 1518135754;
  private static Date EXPIRATION = new Date(TIMESTAMP * 1000);
  private static byte[] KEY_BYTES = Base64.getUrlDecoder().decode("aaaaaaaaaaaaaaaaaaaaaa==");
  private static String KEY_NAME = "my-key";
  private static String BASE_URL = "https://www.example.com/";

  @Test
  public void testUrlPath() throws Exception {
    String result = signUrl(BASE_URL + "foo", KEY_BYTES, KEY_NAME, EXPIRATION);
    final String expected = "https://www.example.com/foo?Expires=1518135754&KeyName=my-key&Signature=vUfG4yv47dyns1j9e_OI6_5meuA=";
    assertEquals(result, expected);
  }

  @Test
  public void testUrlParams() throws Exception {
    String result = signUrl(BASE_URL + "?param=true", KEY_BYTES, KEY_NAME, EXPIRATION);
    final String expected = "https://www.example.com/?param=true&Expires=1518135754&KeyName=my-key&Signature=6TijW8OMX3gcMI5Kqs8ESiPY97c=";
    assertEquals(result, expected);
  }


  @Test
  public void testStandard() throws Exception {
    String result = signUrl(BASE_URL, KEY_BYTES, KEY_NAME, EXPIRATION);
    final String expected = "https://www.example.com/?Expires=1518135754&KeyName=my-key&Signature=4D0AbT4y0O7ZCzCUcAtPOJDkl2g=";
    assertEquals(result, expected);
  }
}
