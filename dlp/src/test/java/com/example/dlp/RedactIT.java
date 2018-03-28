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

package com.example.dlp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
// CHECKSTYLE OFF: AbbreviationAsWordInName
public class RedactIT {

  // CHECKSTYLE ON: AbbreviationAsWordInName
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
  }

  @Test
  public void testRedactImage() throws Exception {
    // InspectIT Tests verify original has PII present
    String outputFilePath = "src/test/resources/output.png";

    // Restrict phone number, but not email
    Redact.main(
        new String[] {
          "-f", "src/test/resources/test.png",
          "-infoTypes", "PHONE_NUMBER",
          "-o", outputFilePath
        });
    bout.reset();

    // Verify that phone_number is missing but email is present
    Inspect.main(
        new String[] {"-f", outputFilePath, "-infoTypes", "PHONE_NUMBER", "EMAIL_ADDRESS"});
    String output = bout.toString();
    assertThat(output, not(containsString("PHONE_NUMBER")));
    assertThat(output, containsString("EMAIL_ADDRESS"));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }
}
