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
public class InspectIT {
  // CHECKSTYLE ON: AbbreviationAsWordInName

  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Update to Google Cloud Storage path containing test.txt
  private String bucketName = System.getenv("GOOGLE_CLOUD_PROJECT") + "/dlp";
  private String topicId = "dlp-tests";
  private String subscriptionId = "dlp-test";

  // Update to Google Cloud Datastore Kind containing an entity
  // with phone number and email address properties.
  private String datastoreKind = "dlp";

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    assertNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
  }

  @Test
  public void testBigqueryInspectionReturnsInfoTypes() throws Exception {
    Inspect.main(
        new String[] {
          "-bq",
          "-datasetId",
          "integration_tests_dlp",
          "-topicId",
          topicId,
          "-subscriptionId",
          subscriptionId,
          "-tableId",
          "harmful",
          "-infoTypes",
          "PHONE_NUMBER",
          "EMAIL_ADDRESS"
        });
    String output = bout.toString();
    assertThat(output, containsString("PHONE_NUMBER"));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }
}
