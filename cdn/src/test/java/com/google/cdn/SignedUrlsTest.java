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

import java.util.Base64;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.cdn.SignedUrls.signUrl;
import static junit.framework.TestCase.assertEquals;


@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SignedUrlsTest {
    private static Date EXPIRATION = new Date((long)1518135754*1000);
    private static byte[] KEY_BYTES = Base64.getUrlDecoder().decode("aaaaaaaaaaaaaaaaaaaaaa==");
    private static String KEY_NAME = "my-key";
    private static String BASE_URL = "https://www.google.com/";

    @Test
    public void testUrlPath() throws Exception {
        String result = signUrl(BASE_URL+"foo", KEY_BYTES, KEY_NAME, EXPIRATION);
        final String expected = "https://www.google.com/foo?Expires=1518135754&KeyName=my-key&Signature=SBdQtypBTcz0gvHRDZjy2pc-F0s=";
        assertEquals(result, expected);
    }

    @Test
    public void testUrlParams() throws Exception {
        String result = signUrl(BASE_URL+"?param=true", KEY_BYTES, KEY_NAME, EXPIRATION);
        final String expected = "https://www.google.com/?param=true&Expires=1518135754&KeyName=my-key&Signature=ilkstIAKFvOlckbVdfZBWAror3o=";
        assertEquals(result, expected);
    }


    @Test
    public void testStandard() throws Exception {
        String result = signUrl(BASE_URL, KEY_BYTES, KEY_NAME, EXPIRATION);
        final String expected = "https://www.google.com/?Expires=1518135754&KeyName=my-key&Signature=yYnIFLMqsuGfpSuo7nf7wk21boM=";
        assertEquals(result, expected);
    }


}
