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

package snippets.healthcare;


import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import snippets.healthcare.datasets.DatasetCreate;
import snippets.healthcare.datasets.DatasetDelete;
import snippets.healthcare.hl7v2.Hl7v2StoreCreate;
import snippets.healthcare.hl7v2.messages.HL7v2MessageCreate;
import snippets.healthcare.hl7v2.messages.HL7v2MessageDelete;
import snippets.healthcare.hl7v2.messages.HL7v2MessageGet;
import snippets.healthcare.hl7v2.messages.HL7v2MessageIngest;
import snippets.healthcare.hl7v2.messages.HL7v2MessageList;
import snippets.healthcare.hl7v2.messages.HL7v2MessagePatch;

@RunWith(JUnit4.class)
public class Hl7v2MessageTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String datasetName;

  private static String hl7v2StoreName;

  private static String messageName;

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @BeforeClass
  public static void setUp() throws IOException {
    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    datasetName =
        String.format("projects/%s/locations/%s/datasets/%s", PROJECT_ID, REGION_ID, datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);
  }

  @AfterClass
  public static void deleteTempItems() throws IOException {
    DatasetDelete.datasetDelete(datasetName);
  }

  @Before
  public void beforeTest() throws IOException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    String hl7v2StoreId = "hl7v2-" + UUID.randomUUID().toString().replaceAll("-", "_");
    hl7v2StoreName = String.format("%s/hl7V2Stores/%s", datasetName, hl7v2StoreId);
    Hl7v2StoreCreate.hl7v2StoreCreate(datasetName, hl7v2StoreId);

    String messageId = "message-" + UUID.randomUUID().toString().replaceAll("-", "_");

    String hl7v2MessagePrefix = hl7v2StoreName + "/messages";

    HL7v2MessageCreate.hl7v2MessageCreate(
        hl7v2StoreName, messageId, "src/test/resources/hl7v2-sample-ingest.txt");

    Pattern messageNamePattern =
        Pattern.compile(String.format("\"(?<messageName>%s/[^/\"]+)\"", hl7v2MessagePrefix));
    Matcher messageNameMatcher = messageNamePattern.matcher(bout.toString());
    assertTrue(messageNameMatcher.find());
    // '=' is encoded for JSON, but won't work for 'get'.
    messageName = messageNameMatcher.group("messageName").replace("\\u003d", "=");
    messageId = messageName.substring(messageName.indexOf("messages/") + "messages/".length());

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void test_HL7v2MessageCreate() throws Exception {
    HL7v2MessageCreate.hl7v2MessageCreate(
        hl7v2StoreName, "new-hl7v2-message", "src/test/resources/hl7v2-sample-ingest.txt");

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message created: "));
  }

  @Test
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void test_GetHL7v2Message() throws Exception {
    HL7v2MessageGet.hl7v2MessageGet(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message retrieved:"));
  }

  @Test
  public void test_Hl7v2MessageList() throws Exception {
    // There can be a delay between when a message is created
    // or ingested and when it's viewable when listing messages
    // in a store, so sleep for 30 seconds while waiting for
    // the message to fully propagate.
    Thread.sleep(30000);
    HL7v2MessageList.hl7v2MessageList(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved "));
  }

  @Test
  public void test_Hl7v2MessagePatch() throws Exception {
    HL7v2MessagePatch.hl7v2MessagePatch(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message patched:"));
  }

  @Test
  public void test_Hl7v2MessageIngest() throws Exception {
    HL7v2MessageIngest.hl7v2MessageIngest(
        hl7v2StoreName, "src/test/resources/hl7v2-sample-ingest.txt");

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message ingested:"));
  }

  @Test
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void test_DeleteHL7v2Message() throws Exception {
    HL7v2MessageDelete.hl7v2MessageDelete(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message deleted."));
  }
}
