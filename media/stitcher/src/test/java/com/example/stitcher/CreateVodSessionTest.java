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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreateVodSessionTest {

  private static final String LOCATION = "us-central1";
  private static final String VOD_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/hls-vod/manifest.m3u8";
  // VMAP Pre-roll
  // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
  private static final String VOD_AD_TAG_URI =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=";
  private static String PROJECT_ID;
  private static String SESSION_NAME;
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
    // Project number is always returned in the VOD session name
    SESSION_NAME = String.format("locations/%s/vodSessions/", LOCATION);
    bout.reset();
  }

  @Test
  public void test_CreateVodSession() throws IOException {
    CreateVodSession.createVodSession(PROJECT_ID, LOCATION, VOD_URI, VOD_AD_TAG_URI);
    String output = bout.toString();
    assertThat(output, containsString(SESSION_NAME));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    // No delete method for a VOD session
    System.setOut(originalOut);
    bout.reset();
  }
}
