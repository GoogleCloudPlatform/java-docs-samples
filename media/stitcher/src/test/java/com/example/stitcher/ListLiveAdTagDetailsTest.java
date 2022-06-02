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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ListLiveAdTagDetailsTest {

  private static final String LOCATION = "us-central1";
  private static final String LIVE_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/hls-live/manifest.m3u8";
  // Single Inline Linear
  // (https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags)
  private static final String LIVE_AD_TAG_URI =
      "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";
  private static final String SLATE_ID =
      "my-slate-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String SLATE_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/ForBiggerEscapes.mp4";
  private static String PROJECT_ID;
  private static String SESSION_ID;
  private static String AD_TAG_DETAILS_NAME;
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

    try {
      DeleteSlate.deleteSlate(PROJECT_ID, LOCATION, SLATE_ID);
    } catch (NotFoundException e) {
      // Don't worry if the slate doesn't already exist.
    }
    CreateSlate.createSlate(PROJECT_ID, LOCATION, SLATE_ID, SLATE_URI);
    bout.reset();

    CreateLiveSession.createLiveSession(PROJECT_ID, LOCATION, LIVE_URI, LIVE_AD_TAG_URI, SLATE_ID);
    String output = bout.toString();
    Matcher idMatcher =
        Pattern.compile(
                String.format(
                    "Created live session: projects/.*/locations/%s/liveSessions/(.*)", LOCATION))
            .matcher(output);
    if (idMatcher.find()) {
      SESSION_ID = idMatcher.group(1);
    }
    // Project number is always returned in the live session name
    AD_TAG_DETAILS_NAME =
        String.format("locations/%s/liveSessions/%s/liveAdTagDetails/", LOCATION, SESSION_ID);

    // To get ad tag details, you need to curl the main manifest and
    // a rendition first. This supplies media player information to the API.
    //
    // Curl the playUri first. The last line of the response will contain a
    // renditions location. Curl the live session name with the rendition
    // location appended.

    String playUri = TestUtils.getPlayUri(output);
    assertNotNull(playUri);
    String renditions = TestUtils.getRenditions(playUri);
    assertNotNull(renditions);

    // playUri will be in the following format:
    // https://videostitcher.googleapis.com/v1/projects/{project}/locations/{location}/liveSessions/{session-id}/manifest.m3u8?signature=...
    // Replace manifest.m3u8?signature=... with the renditions location.
    String renditionsUri =
        String.format("%s/%s", playUri.substring(0, playUri.lastIndexOf("/")), renditions);
    TestUtils.connectToRenditionsUrl(renditionsUri);
    bout.reset();
  }

  @Test
  public void test_ListLiveAdTagDetails() throws IOException {
    ListLiveAdTagDetails.listLiveAdTagDetails(PROJECT_ID, LOCATION, SESSION_ID);
    String output = bout.toString();
    assertThat(output, containsString(AD_TAG_DETAILS_NAME));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    DeleteSlate.deleteSlate(PROJECT_ID, LOCATION, SLATE_ID);
    // No delete method for a live session
    System.setOut(originalOut);
    bout.reset();
  }
}
