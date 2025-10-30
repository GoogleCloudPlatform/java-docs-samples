/*
 * Copyright 2025 Google LLC
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

package genai.live;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LiveIT {

  private static final String GEMINI_FLASH_LIVE_PREVIEW = "gemini-2.0-flash-live-preview-04-09";
  private static final String GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO =
      "gemini-live-2.5-flash-preview-native-audio";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testLiveCodeExecWithTxt() {
    LiveCodeExecWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    String output = bout.toString();
    assertThat(output).contains("> Compute the largest prime palindrome under 100000");
    assertThat(output).contains("text:");
    assertThat(output).contains("code: ExecutableCode{code=");
    assertThat(output).contains("result: CodeExecutionResult{outcome");
    assertThat(output).contains("The model is done generating.");
  }

  @Test
  public void testLiveGroundGoogSearchWithTxt() {
    LiveGroundGoogSearchWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    String output = bout.toString();
    assertThat(output).contains("> When did the last Brazil vs. Argentina soccer match happen?");
    assertThat(output).contains("Output:");
    assertThat(output).contains("The model is done generating.");
  }

  @Test
  public void testLiveTranscribeWithAudio() {
    LiveTranscribeWithAudio.generateContent(GEMINI_FLASH_LIVE_PREVIEW_NATIVE_AUDIO);
    String output = bout.toString();
    assertThat(output).contains("> Hello? Gemini, are you there?");
    assertThat(output).contains("Model turn:");
    assertThat(output).contains("Output transcript:");
    assertThat(output).contains("The model is done generating.");
  }

  @Test
  public void testLiveWithTxt() {
    LiveWithTxt.generateContent(GEMINI_FLASH_LIVE_PREVIEW);
    String output = bout.toString();
    assertThat(output).contains("> Hello? Gemini, are you there?");
    assertThat(output).contains("Output:");
    assertThat(output).contains("The model is done generating.");
  }
}
