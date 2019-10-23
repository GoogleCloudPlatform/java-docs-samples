/*
 * Copyright 2019 Google LLC
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

package com.example.translate;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TranslateV3TranslateTextWithModelTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_PROJECT_ID");
  private static final String MODEL_ID = "TRL2188848820815848149";

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testTranslateTextWithModel() {
    // Act
    TranslateV3TranslateTextWithModel.sampleTranslateTextWithModel(
            MODEL_ID,
            "That' il do it.",
            "ja",
            "en",
            PROJECT_ID,
            "us-central1");

    // Assert
    String got = bout.toString();

    assertThat(got, anyOf(containsString("それはそうだ"), containsString("それじゃあ")));
  }
}