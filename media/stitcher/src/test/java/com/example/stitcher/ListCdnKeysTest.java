/*
 * Copyright 2022 Google LLC
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

package com.example.stitcher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ListCdnKeysTest {

  private static final String LOCATION = "us-central1";
  private static final String GCDN_KEY_ID =
      "my-test-google-cdn-key-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String AKAMAI_KEY_ID =
      "my-test-akamai-cdn-key-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String HOSTNAME = "cdn.example.com";
  private static final String GCDN_KEYNAME = "gcdn-key"; // field in the GCDN key
  private static final String GCDN_PRIVATE_KEY = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==";
  private static final String AKAMAI_TOKEN_KEY = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==";
  private static String PROJECT_ID;
  private static String GCDN_KEY_NAME; // resource name for the Google CDN key
  private static String AKAMAI_KEY_NAME; // resource name for the Akamai CDN key
  private static PrintStream originalOut;
  private ByteArrayOutputStream bout;

  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest() throws IOException {
    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Google CDN key
    GCDN_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", LOCATION, GCDN_KEY_ID);
    try {
      DeleteCdnKey.deleteCdnKey(PROJECT_ID, LOCATION, GCDN_KEY_ID);
    } catch (NotFoundException e) {
      // Don't worry if the key doesn't already exist.
    }
    CreateCdnKey.createCdnKey(
        PROJECT_ID, LOCATION, GCDN_KEY_ID, HOSTNAME, GCDN_KEYNAME, GCDN_PRIVATE_KEY, "");

    // Akamai CDN key
    AKAMAI_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", LOCATION, AKAMAI_KEY_ID);
    try {
      DeleteCdnKey.deleteCdnKey(PROJECT_ID, LOCATION, AKAMAI_KEY_ID);
    } catch (NotFoundException e) {
      // Don't worry if the key doesn't already exist.
    }
    CreateCdnKey.createCdnKey(
        PROJECT_ID, LOCATION, AKAMAI_KEY_ID, HOSTNAME, "", "", AKAMAI_TOKEN_KEY);

    bout.reset();
  }

  @Test
  public void test_ListCdnKeys() throws IOException {
    // Google CDN key and Amamai CDN key should be present
    ListCdnKeys.listCdnKeys(PROJECT_ID, LOCATION);
    String output = bout.toString();
    assertThat(output, containsString(GCDN_KEY_NAME));
    assertThat(output, containsString(AKAMAI_KEY_NAME));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, LOCATION, GCDN_KEY_ID);
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, LOCATION, AKAMAI_KEY_ID);
    System.setOut(originalOut);
    bout.reset();
  }
}
