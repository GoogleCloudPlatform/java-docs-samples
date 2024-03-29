/*
 * Copyright 2017 Google Inc.
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

package com.example.vision;

import static com.google.common.truth.Truth.assertThat;

import com.example.vision.snippets.DetectCropHints;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DetectCropHintsTest {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  @Ignore("TODO: Remove after fixing https://github.com/GoogleCloudPlatform/java-docs-samples/issues/8977")
  public void testCropHints() throws Exception {
    // Act
    DetectCropHints.detectCropHints("./resources/wakeupcat.jpg");

    // Assert
    String got = bout.toString();
    assertThat(got).contains("vertices {");
    assertThat(got).containsMatch(Pattern.compile("x: 2\\d{2}"));
    assertThat(got).containsMatch(Pattern.compile("y: 4\\d{2}"));
  }
}
