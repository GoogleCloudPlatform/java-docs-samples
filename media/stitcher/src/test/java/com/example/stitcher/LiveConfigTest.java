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

package com.example.stitcher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.cloud.video.stitcher.v1.LiveConfig;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListLiveConfigsPagedResponse;
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
public class LiveConfigTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String SLATE_ID = TestUtils.getSlateId();
  private static final String LIVE_CONFIG_ID = TestUtils.getLiveConfigId();
  private static String LIVE_CONFIG_NAME;
  private static String PROJECT_ID;
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

    TestUtils.cleanStaleSlates(PROJECT_ID, TestUtils.LOCATION);
    TestUtils.cleanStaleLiveConfigs(PROJECT_ID, TestUtils.LOCATION);

    CreateSlate.createSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID, TestUtils.SLATE_URI);

    LIVE_CONFIG_NAME =
        String.format("locations/%s/liveConfigs/%s", TestUtils.LOCATION, LIVE_CONFIG_ID);
    LiveConfig response =
        CreateLiveConfig.createLiveConfig(PROJECT_ID, TestUtils.LOCATION, LIVE_CONFIG_ID,
            TestUtils.LIVE_URI,
            TestUtils.LIVE_AD_TAG_URI, SLATE_ID);
    assertThat(response.getName(), containsString(LIVE_CONFIG_NAME));
  }

  @Test
  public void testGetLiveConfig() throws IOException {
    LiveConfig response = GetLiveConfig.getLiveConfig(PROJECT_ID, TestUtils.LOCATION,
        LIVE_CONFIG_ID);
    assertThat(response.getName(), containsString(LIVE_CONFIG_NAME));
  }

  @Test
  public void testListLiveConfigs() throws IOException {
    ListLiveConfigsPagedResponse response =
        ListLiveConfigs.listLiveConfigs(PROJECT_ID, TestUtils.LOCATION);
    Boolean pass = false;
    for (LiveConfig liveConfig : response.iterateAll()) {
      if (liveConfig.getName().contains(LIVE_CONFIG_NAME)) {
        pass = true;
        break;
      }
    }
    assert (pass);
  }

  @After
  public void tearDown() {
    bout.reset();
  }

  @AfterClass
  public static void afterTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteLiveConfig.deleteLiveConfig(PROJECT_ID, TestUtils.LOCATION, LIVE_CONFIG_ID);
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted live config"));
    bout.reset();

    DeleteSlate.deleteSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID);
    deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted slate"));

    System.out.flush();
    System.setOut(originalOut);
  }
}
