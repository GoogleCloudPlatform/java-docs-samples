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

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TranslateV3GetGlossaryTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_PROJECT_ID");
  private static final String GLOSSARY_INPUT_URI =
          "gs://cloud-samples-data/translation/glossary_ja.csv";

  private String glossaryId;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  
  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    glossaryId = String.format("must_start_with_letter_%s",
            UUID.randomUUID().toString().replace("-", "_").substring(0, 26));

    // Setup
    TranslateV3CreateGlossary.sampleCreateGlossary(
            PROJECT_ID, glossaryId, GLOSSARY_INPUT_URI);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    TranslateV3DeleteGlossary.sampleDeleteGlossary(PROJECT_ID, glossaryId);
    System.setOut(null);
  }

  @Test
  public void testGetGlossary() {
    // Act
    TranslateV3GetGlossary.sampleGetGlossary(PROJECT_ID, glossaryId);
    String got = bout.toString();

    // Assert
    assertThat(got).contains(glossaryId);
    assertThat(got).contains(GLOSSARY_INPUT_URI);
  }

}