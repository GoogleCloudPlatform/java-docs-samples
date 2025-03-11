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

// Tests for Gen AI SDK code samples.

package genai.text_generation;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextGenerationIT {

  private ByteArrayOutputStream bout;
  private PrintStream originalOut;


  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @Before
  public void setUp() {
      originalOut = System.out;
      bout = new ByteArrayOutputStream();
      System.setOut(new PrintStream(bout));
  }

    public void tearDown() {
        System.setOut(originalOut);
    }

  @Test
  public void testTextgenWithTxt() throws IOException, HttpException {
    TxtgenWithTxt.main(new String[]{});
    tearDown();
    assertThat(bout.toString()).isNotEmpty();
  }
}
