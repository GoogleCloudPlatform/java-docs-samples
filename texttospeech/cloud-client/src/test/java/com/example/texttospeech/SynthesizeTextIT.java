/*
 * Copyright 2018 Google Inc.
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

package com.example.texttospeech;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for SynthesizeText sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SynthesizeTextIT {

  private static String OUTPUT = "output.mp3";
  private static String TEXT = "Hello there.";
  private static String SSML = "<?xml version=\"1.0\"?>\n"
      + "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\"\n"
      + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
      + "xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis\n"
      + "http://www.w3.org/TR/speech-synthesis/synthesis.xsd\" xml:lang=\"en-US\">\n"
      + "Hello there.\n"
      + "</speak>";

  private File outputFile;

  @After
  public void tearDown() {
    outputFile.delete();
  }

  @Test
  public void testSynthesizeText() throws Exception {
    // Act
    SynthesizeText.synthesizeText(TEXT);

    // Assert
    outputFile = new File(OUTPUT);
    assertThat(outputFile.isFile()).isTrue();
  }

  @Test
  public void testSynthesizeSsml() throws Exception {
    // Act
    SynthesizeText.synthesizeText(SSML);

    // Assert
    outputFile = new File(OUTPUT);
    assertThat(outputFile.isFile()).isTrue();
  }
}