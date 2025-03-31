/*
 * Copyright 2024 Google LLC
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
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListVodConfigsPagedResponse;
import com.google.cloud.video.stitcher.v1.VodConfig;
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
public class VodConfigTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String VOD_CONFIG_ID = TestUtils.getVodConfigId();
  private static String VOD_CONFIG_NAME;
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

    TestUtils.cleanStaleVodConfigs(PROJECT_ID, TestUtils.LOCATION);

    VOD_CONFIG_NAME =
        String.format("locations/%s/vodConfigs/%s", TestUtils.LOCATION, VOD_CONFIG_ID);
    VodConfig response =
        CreateVodConfig.createVodConfig(
            PROJECT_ID,
            TestUtils.LOCATION,
            VOD_CONFIG_ID,
            TestUtils.VOD_URI,
            TestUtils.VOD_AD_TAG_URI);
    assertThat(response.getName(), containsString(VOD_CONFIG_NAME));
  }

  @Test
  public void testGetVodConfig() throws IOException {
    VodConfig response = GetVodConfig.getVodConfig(PROJECT_ID, TestUtils.LOCATION, VOD_CONFIG_ID);
    assertThat(response.getName(), containsString(VOD_CONFIG_NAME));
  }

  @Test
  public void testListVodConfigs() throws IOException {
    ListVodConfigsPagedResponse response =
        ListVodConfigs.listVodConfigs(PROJECT_ID, TestUtils.LOCATION);
    Boolean pass = false;
    for (VodConfig vodConfig : response.iterateAll()) {
      if (vodConfig.getName().contains(VOD_CONFIG_NAME)) {
        pass = true;
        break;
      }
    }
    assert (pass);
  }

  @Test
  public void testUpdateVodConfig()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    VodConfig response =
        UpdateVodConfig.updateVodConfig(
            PROJECT_ID, TestUtils.LOCATION, VOD_CONFIG_ID, TestUtils.UPDATED_VOD_URI);
    assertThat(response.getName(), containsString(VOD_CONFIG_NAME));
    assertThat(response.getSourceUri(), containsString(TestUtils.UPDATED_VOD_URI));
  }

  @After
  public void tearDown() {
    bout.reset();
  }

  @AfterClass
  public static void afterTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteVodConfig.deleteVodConfig(PROJECT_ID, TestUtils.LOCATION, VOD_CONFIG_ID);
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted VOD config"));
    System.out.flush();
    System.setOut(originalOut);
  }
}
