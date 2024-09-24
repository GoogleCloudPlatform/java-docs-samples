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

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListCdnKeysPagedResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CdnKeyTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String CLOUD_CDN_KEY_ID = TestUtils.getCdnKeyId();
  private static final String MEDIA_CDN_KEY_ID = TestUtils.getCdnKeyId();
  private static final String AKAMAI_KEY_ID = TestUtils.getCdnKeyId();
  private static String PROJECT_ID;
  private static String CLOUD_CDN_KEY_NAME; // resource name for the Cloud CDN key
  private static String MEDIA_CDN_KEY_NAME; // resource name for the Media CDN key
  private static String AKAMAI_KEY_NAME; // resource name for the Akamai CDN key
  private static final String UPDATED_CLOUD_CDN_PRIVATE_KEY =
      "VGhpcyBpcyBhbiB1cGRhdGVkIHRlc3Qgc3RyaW5nLg==";
  private static final String UPDATED_MEDIA_CDN_PRIVATE_KEY =
      "ZZZzNDU2Nzg5MDEyMzQ1Njc4OTAxzg5MDEyMzQ1Njc4OTAxMjM0NTY3DkwMTIZZZ";
  private static final String UPDATED_AKAMAI_TOKEN_KEY =
      "VGhpcyBpcyBhbiB1cGRhdGVkIHRlc3Qgc3RyaW5nLg==";

  private static PrintStream originalOut;
  private static ByteArrayOutputStream bout;


  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  @BeforeClass
  public static void beforeTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");

    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    TestUtils.cleanStaleCdnKeys(PROJECT_ID, TestUtils.LOCATION);

    // Cloud CDN key
    CLOUD_CDN_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", TestUtils.LOCATION,
        CLOUD_CDN_KEY_ID);
    // Media CDN key
    MEDIA_CDN_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", TestUtils.LOCATION,
        MEDIA_CDN_KEY_ID);
    // Akamai CDN key
    AKAMAI_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", TestUtils.LOCATION, AKAMAI_KEY_ID);

    // Cloud CDN key
    CdnKey response = CreateCdnKey.createCdnKey(
        PROJECT_ID, TestUtils.LOCATION, CLOUD_CDN_KEY_ID, TestUtils.HOSTNAME, TestUtils.KEYNAME,
        TestUtils.CLOUD_CDN_PRIVATE_KEY, false);
    assertThat(response.getName(), containsString(CLOUD_CDN_KEY_NAME));

    // Media CDN key
    response = CreateCdnKey.createCdnKey(
        PROJECT_ID, TestUtils.LOCATION, MEDIA_CDN_KEY_ID, TestUtils.HOSTNAME, TestUtils.KEYNAME,
        TestUtils.MEDIA_CDN_PRIVATE_KEY, true);
    assertThat(response.getName(), containsString(MEDIA_CDN_KEY_NAME));

    // Akamai CDN key
    response = CreateCdnKeyAkamai.createCdnKeyAkamai(
        PROJECT_ID, TestUtils.LOCATION, AKAMAI_KEY_ID, TestUtils.HOSTNAME,
        TestUtils.AKAMAI_TOKEN_KEY);
    assertThat(response.getName(), containsString(AKAMAI_KEY_NAME));
  }

  @Test
  public void testGetCdnKey() throws IOException {
    // Cloud CDN key
    CdnKey response = GetCdnKey.getCdnKey(PROJECT_ID, TestUtils.LOCATION, CLOUD_CDN_KEY_ID);
    assertThat(response.getName(), containsString(CLOUD_CDN_KEY_NAME));

    // Media CDN key
    response = GetCdnKey.getCdnKey(PROJECT_ID, TestUtils.LOCATION, MEDIA_CDN_KEY_ID);
    assertThat(response.getName(), containsString(MEDIA_CDN_KEY_NAME));

    // Akamai CDN key
    response = GetCdnKey.getCdnKey(PROJECT_ID, TestUtils.LOCATION, AKAMAI_KEY_ID);
    assertThat(response.getName(), containsString(AKAMAI_KEY_NAME));
  }

  @Test
  public void testListCdnKeys() throws IOException {
    // Cloud, Media, and Akamai CDN keys should be present
    ListCdnKeysPagedResponse response =
        ListCdnKeys.listCdnKeys(PROJECT_ID, TestUtils.LOCATION);
    Boolean cloud = false;
    Boolean media = false;
    Boolean akamai = false;

    for (CdnKey cdnKey : response.iterateAll()) {
      if (cdnKey.getName().contains(CLOUD_CDN_KEY_NAME)) {
        cloud = true;
      } else if (cdnKey.getName().contains(MEDIA_CDN_KEY_NAME)) {
        media = true;
      } else if (cdnKey.getName().contains(AKAMAI_KEY_NAME)) {
        akamai = true;
      }
    }
    assert (cloud && media && akamai);
  }

  @Test
  public void testUpdateCdnKey()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Cloud CDN key
    CdnKey response = UpdateCdnKey.updateCdnKey(
        PROJECT_ID,
        TestUtils.LOCATION,
        CLOUD_CDN_KEY_ID,
        TestUtils.UPDATED_HOSTNAME,
        TestUtils.KEYNAME,
        UPDATED_CLOUD_CDN_PRIVATE_KEY,
        false);
    assertThat(response.getName(), containsString(CLOUD_CDN_KEY_NAME));

    // Media CDN key
    response = UpdateCdnKey.updateCdnKey(
        PROJECT_ID,
        TestUtils.LOCATION,
        MEDIA_CDN_KEY_ID,
        TestUtils.UPDATED_HOSTNAME,
        TestUtils.KEYNAME,
        UPDATED_MEDIA_CDN_PRIVATE_KEY,
        true);
    assertThat(response.getName(), containsString(MEDIA_CDN_KEY_NAME));

    // Akamai CDN key
    response = UpdateCdnKeyAkamai.updateCdnKeyAkamai(
        PROJECT_ID, TestUtils.LOCATION, AKAMAI_KEY_ID, TestUtils.UPDATED_HOSTNAME,
        UPDATED_AKAMAI_TOKEN_KEY);
    assertThat(response.getName(), containsString(AKAMAI_KEY_NAME));
  }

  @After
  public void tearDown() {
    bout.reset();
  }

  @AfterClass
  public static void afterTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Cloud CDN key
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, TestUtils.LOCATION, CLOUD_CDN_KEY_ID);
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted CDN key"));
    bout.reset();

    // Media CDN key
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, TestUtils.LOCATION, MEDIA_CDN_KEY_ID);
    deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted CDN key"));
    bout.reset();

    // Akamai CDN key
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, TestUtils.LOCATION, AKAMAI_KEY_ID);
    deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted CDN key"));

    System.out.flush();
    System.setOut(originalOut);
  }
}
