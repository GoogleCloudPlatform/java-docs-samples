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

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
// CHECKSTYLE OFF: AbbreviationAsWordInName
public class DeIdentificationIT {

  // CHECKSTYLE ON: AbbreviationAsWordInName
  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Update to wrapped local encryption key
  private String wrappedKey = System.getenv("DLP_DEID_WRAPPED_KEY");

  // Update to name of KMS key used to wrap local encryption key
  private String keyName = System.getenv("DLP_DEID_KEY_NAME");

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out); // TODO(b/64541432) DLP currently doesn't support GOOGLE DEFAULT AUTH
    assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    assertNotNull(System.getenv("DLP_DEID_WRAPPED_KEY"));
    assertNotNull(System.getenv("DLP_DEID_KEY_NAME"));
  }

  @Test
  public void testDeidStringMasksCharacters() throws Exception {
    String text = "\"My SSN is 372819127\"";
    DeIdentification.main(
        new String[] {
          "-m", text,
          "-maskingCharacter", "x",
          "-numberToMask", "5"
        });
    String output = bout.toString();
    assertThat(output, containsString("My SSN is xxxxx9127"));
  }

  @Test
  public void testDeidStringPerformsFpe() throws Exception {
    String text = "\"My SSN is 372819127\"";
    DeIdentification.main(
        new String[] {
          "-f", text,
          "-wrappedKey", wrappedKey,
          "-keyName", keyName
        });
    String output = bout.toString();
    assertFalse(
        "Response contains original SSN.",
        output.contains("372819127"));
    assertThat(output, containsString("My SSN is "));
  }

  @Test
  public void testDeidentifyWithDateShift() throws Exception {
    DeIdentification.main(
        new String[] {
            "-d",
            "-inputCsvPath", "src/test/resources/dates.csv",
            "-outputCsvPath", "src/test/resources/results.temp.csv",
           "-dateFields", "birth_date,register_date",
            "-lowerBoundDays", "5",
            "-upperBoundDays", "5",
            "-contextField", "name",
            "-wrappedKey", wrappedKey,
            "-keyName", keyName
        });
    String output = bout.toString();
    assertThat(
        output, containsString("Successfully saved date-shift output to: results.temp.csv"));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }
}
