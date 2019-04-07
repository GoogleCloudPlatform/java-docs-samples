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

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import snippets.healthcare.datasets.DatasetCreate;
import snippets.healthcare.hl7v2.Hl7v2StoreCreate;
import snippets.healthcare.hl7v2.Hl7v2StoreDelete;
import snippets.healthcare.hl7v2.Hl7v2StoreGet;
import snippets.healthcare.hl7v2.Hl7v2StoreGetIamPolicy;
import snippets.healthcare.hl7v2.Hl7v2StoreList;
import snippets.healthcare.hl7v2.Hl7v2StoreSetIamPolicy;
import snippets.healthcare.hl7v2.messages.Hl7v2MessageCreate;
import snippets.healthcare.hl7v2.messages.Hl7v2MessageDelete;
import snippets.healthcare.hl7v2.messages.Hl7v2MessageGet;
import snippets.healthcare.hl7v2.messages.Hl7v2MessageIngest;
import snippets.healthcare.hl7v2.messages.Hl7v2MessageList;
import snippets.healthcare.hl7v2.messages.Hl7v2MessagePatch;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Hl7v2StoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = System.getenv("GCLOUD_BUCKET_NAME");
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String datasetId;
  private static String datasetName;

  private static String hl7v2StoreId;
  private static String hl7v2StoreName;

  private static String messageId;
  private static String messageName;

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
    requireEnvVar("GCLOUD_BUCKET_NAME");
    requireEnvVar("GCLOUD_PUBSUB_TOPIC");
  }

  @BeforeClass
  public static void setUp() throws IOException {
    datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    datasetName =
        String.format("projects/%s/locations/%s/datasets/%s", PROJECT_ID, REGION_ID, datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    hl7v2StoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    hl7v2StoreName = String.format("%s/dicomStores/%s", datasetName, hl7v2StoreId);

    messageId = "message-" + UUID.randomUUID().toString().replaceAll("-", "_");
    messageName = String.format("/messages/%s", hl7v2StoreName, messageId);
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void test_01_Hl7v2StoreCreate() throws Exception {
    Hl7v2StoreCreate.hl7v2StoreCreate(datasetName, hl7v2StoreId);

    String output = bout.toString();
    assertThat(output, containsString("Hl7V2Store store created:"));
  }

  @Test
  public void test_02_Hl7v2StoreGet() throws Exception {
    Hl7v2StoreGet.hl7v2eStoreGet(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("Hl7V2Store store created:"));
  }

  @Test
  public void test_02_Hl7v2StoreGetIamPolicy() throws Exception {
    Hl7v2StoreGetIamPolicy.hl7v2StoreGetIamPolicy(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 store IAMPolicy retrieved:"));
  }

  @Test
  public void test_02_Hl7v2StoreSetIamPolicy() throws Exception {
    Hl7v2StoreSetIamPolicy.hl7v2StoreSetIamPolicy(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 policy has been updated:"));
  }

  @Test
  public void test_02_Hl7v2StoreList() throws Exception {
    Hl7v2StoreList.hl7v2StoreList(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved"));
  }

  @Test
  public void test_02_HL7v2MessageCreate() throws Exception {
    Hl7v2MessageCreate.hl7v2MessageCreate(hl7v2StoreName, messageId);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message created: "));
  }

  @Test
  public void test_03_GetHL7v2Message() throws Exception {
    Hl7v2MessageGet.hl7v2MessageGet(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message retrieved:"));
  }

  @Test
  public void test_03_Hl7v2MessageList() throws Exception {
    Hl7v2MessageList.hl7v2MessageList(messageName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved "));
  }

  @Test
  public void test_03_Hl7v2MessagePatch() throws Exception {
    Hl7v2MessagePatch.hl7v2MessagePatch(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message patched:"));
  }

  @Test
  public void test_04_Hl7v2MessageIngest() throws Exception {
    Hl7v2MessageIngest.hl7v2MessageIngest(
        messageName, "src/test/resources/hl7v2-sample-ingest.txt");

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message ingested:"));
  }

  @Test
  public void test_05_DeleteHL7v2Message() throws Exception {
    Hl7v2MessageDelete.hl7v2MessageDelete(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 message deleted."));
  }

  @Test
  public void test_06_Hl7v2StoreDelete() throws Exception {
    Hl7v2StoreDelete.hl7v2StoreDelete(messageName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 store deleted."));
  }
}
