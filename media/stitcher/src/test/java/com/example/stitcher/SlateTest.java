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
import com.google.cloud.video.stitcher.v1.Slate;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient.ListSlatesPagedResponse;
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
public class SlateTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String SLATE_ID = TestUtils.getSlateId();
  private static String SLATE_NAME;
  private static final String UPDATED_SLATE_URI =
      "https://storage.googleapis.com/cloud-samples-data/media/ForBiggerJoyrides.mp4";
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

    SLATE_NAME =
        String.format("locations/%s/slates/%s", TestUtils.LOCATION, SLATE_ID);
    Slate response =
        CreateSlate.createSlate(
            PROJECT_ID,
            TestUtils.LOCATION,
            SLATE_ID,
            TestUtils.SLATE_URI);
    assertThat(response.getName(), containsString(SLATE_NAME));
  }

  @Test
  public void testGetSlate() throws IOException {
    Slate response = GetSlate.getSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID);
    assertThat(response.getName(), containsString(SLATE_NAME));
  }

  @Test
  public void testListSlates() throws IOException {
    ListSlatesPagedResponse response =
        ListSlates.listSlates(PROJECT_ID, TestUtils.LOCATION);
    Boolean pass = false;
    for (Slate slate : response.iterateAll()) {
      if (slate.getName().contains(SLATE_NAME)) {
        pass = true;
        break;
      }
    }
    assert (pass);
  }

  @Test
  public void testUpdateSlate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Slate response =
        UpdateSlate.updateSlate(
            PROJECT_ID, TestUtils.LOCATION, SLATE_ID, UPDATED_SLATE_URI);
    assertThat(response.getName(), containsString(SLATE_NAME));
    assertThat(response.getUri(), containsString(UPDATED_SLATE_URI));
  }

  @After
  public void tearDown() {
    bout.reset();
  }

  @AfterClass
  public static void afterTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteSlate.deleteSlate(PROJECT_ID, TestUtils.LOCATION, SLATE_ID);
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted slate"));
    System.out.flush();
    System.setOut(originalOut);
  }
}
