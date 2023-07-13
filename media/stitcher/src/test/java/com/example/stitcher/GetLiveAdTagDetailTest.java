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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GetLiveAdTagDetailTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String SLATE_ID = TestUtils.getSlateId();
  private static final String LIVE_CONFIG_ID = TestUtils.getLiveConfigId();
  private static String PROJECT_ID;
  private static String SESSION_ID;
  private static String AD_TAG_DETAIL_ID;
  private static String AD_TAG_DETAIL_NAME;
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
  public void beforeTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    TestUtils.cleanStaleSlates(PROJECT_ID, TestUtils.LOCATION);
    TestUtils.cleanStaleLiveConfigs(PROJECT_ID, TestUtils.LOCATION);
    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    CreateSlate.createSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID, TestUtils.SLATE_URI);
    CreateLiveConfig.createLiveConfig(PROJECT_ID, TestUtils.LOCATION, LIVE_CONFIG_ID,
        TestUtils.LIVE_URI, TestUtils.LIVE_AD_TAG_URI, SLATE_ID);
    bout.reset();

    CreateLiveSession.createLiveSession(PROJECT_ID, TestUtils.LOCATION, LIVE_CONFIG_ID);
    String output = bout.toString();
    Matcher idMatcher =
        Pattern.compile(
                String.format(
                    "Created live session: projects/.*/locations/%s/liveSessions/(.*)",
                    TestUtils.LOCATION))
            .matcher(output);
    if (idMatcher.find()) {
      SESSION_ID = idMatcher.group(1);
    }

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

    // Project number is always returned in the live session name
    String adTagDetailPrefix =
        String.format("locations/%s/liveSessions/%s/liveAdTagDetails/", TestUtils.LOCATION,
            SESSION_ID);
    ListLiveAdTagDetails.listLiveAdTagDetails(PROJECT_ID, TestUtils.LOCATION, SESSION_ID);
    idMatcher =
        Pattern.compile(String.format("name: \"projects/.*/%s(.*)\"", adTagDetailPrefix))
            .matcher(bout.toString());
    if (idMatcher.find()) {
      AD_TAG_DETAIL_ID = idMatcher.group(1);
      AD_TAG_DETAIL_NAME = adTagDetailPrefix + AD_TAG_DETAIL_ID;
    }

    bout.reset();
  }

  @Test
  public void test_GetLiveAdTagDetail() throws IOException {
    GetLiveAdTagDetail.getLiveAdTagDetail(PROJECT_ID, TestUtils.LOCATION, SESSION_ID,
        AD_TAG_DETAIL_ID);
    String output = bout.toString();
    assertThat(output, containsString(AD_TAG_DETAIL_NAME));
    bout.reset();
  }

  @After
  public void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteSlate.deleteSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID);
    DeleteLiveConfig.deleteLiveConfig(PROJECT_ID, TestUtils.LOCATION, LIVE_CONFIG_ID);
    // No delete method for a live session or ad tag detail
    System.setOut(originalOut);
    bout.reset();
  }
}
