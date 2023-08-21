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
public class GetVodAdTagDetailTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
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
  public void beforeTest() throws IOException {
    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    CreateVodSession.createVodSession(PROJECT_ID, TestUtils.LOCATION, TestUtils.VOD_URI,
        TestUtils.VOD_AD_TAG_URI);
    String output = bout.toString();
    String[] arr = output.split("/");
    SESSION_ID = arr[arr.length - 1].replace("\n", "");
    String sessionName = String.format("locations/%s/vodSessions/%s/", TestUtils.LOCATION,
        SESSION_ID);
    bout.reset();

    ListVodAdTagDetails.listVodAdTagDetails(PROJECT_ID, TestUtils.LOCATION, SESSION_ID);
    Matcher idMatcher =
        Pattern.compile("name: \"projects/.*/" + sessionName + "vodAdTagDetails/(.*)\"")
            .matcher(bout.toString());
    if (idMatcher.find()) {
      AD_TAG_DETAIL_ID = idMatcher.group(1);
      AD_TAG_DETAIL_NAME = sessionName + "vodAdTagDetails/" + AD_TAG_DETAIL_ID;
    }
    bout.reset();
  }

  @Test
  public void test_GetVodAdTagDetailTest() throws IOException {
    GetVodAdTagDetail.getVodAdTagDetail(PROJECT_ID, TestUtils.LOCATION, SESSION_ID,
        AD_TAG_DETAIL_ID);
    String output = bout.toString();
    assertThat(output, containsString(AD_TAG_DETAIL_NAME));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    // No delete method for a VOD session
    System.setOut(originalOut);
    bout.reset();
  }
}
