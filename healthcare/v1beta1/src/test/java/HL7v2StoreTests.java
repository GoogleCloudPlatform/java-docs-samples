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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.healthcare.datasets.DatasetCreate;
import com.google.healthcare.hl7v2.HL7v2Create;
import com.google.healthcare.hl7v2.HL7v2Delete;
import com.google.healthcare.hl7v2.HL7v2Get;
import com.google.healthcare.hl7v2.HL7v2GetIamPolicy;
import com.google.healthcare.hl7v2.HL7v2List;
import com.google.healthcare.hl7v2.HL7v2Patch;
import com.google.healthcare.hl7v2.HL7v2SetIamPolicy;
import com.google.healthcare.hl7v2.messages.HL7v2MessageCreate;
import com.google.healthcare.hl7v2.messages.HL7v2MessageDelete;
import com.google.healthcare.hl7v2.messages.HL7v2MessageGet;
import com.google.healthcare.hl7v2.messages.HL7v2MessageIngest;
import com.google.healthcare.hl7v2.messages.HL7v2MessageList;
import com.google.healthcare.hl7v2.messages.HL7v2MessagePatch;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HL7v2StoreTests extends HealthcareTestBase {
  private static String hl7v2StoreId;
  private static String hl7v2StoreName;
  private static String hl7v2MessageId;
  private static String hl7v2MessagePrefix;
  private static String hl7v2MessageName;

  @BeforeClass
  public static void setUp() throws IOException {
    setUpClass();

    hl7v2StoreId = "hl7v2-store-" + suffix;

    hl7v2StoreName = String.format(
        "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        hl7v2StoreId);

    hl7v2MessagePrefix = String.format(
        "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s/messages",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        hl7v2StoreId);

    DatasetCreate.createDataset(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
  }

  @AfterClass
  public static void tearDownTestClass() throws IOException {
    tearDownClass();
  }

  @After
  public void tearDownTest() {
    tearDown();
  }

  @Test
  public void test_01_CreateHL7v2Store() throws Exception {
    HL7v2Create.createHL7v2Store(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId, hl7v2StoreId);
    assertBoutContents("Created HL7v2 store:", hl7v2StoreName);
  }

  @Test
  public void test_02_GetHL7v2Store() throws Exception {
    HL7v2Get.getHL7v2Store(hl7v2StoreName);
    assertBoutContents("Retrieved HL7v2 store:", hl7v2StoreName);
  }

  @Test
  public void test_02_GetHL7v2StoreIamPolicy() throws Exception {
    HL7v2GetIamPolicy.getIamPolicy(hl7v2StoreName);
    assertBoutContents(".*etag.*");
  }

  @Test
  public void test_02_SetHL7v2StoreIamPolicy() throws Exception {
    HL7v2SetIamPolicy.setIamPolicy(
        hl7v2StoreName,
        "roles/healthcare.hl7V2StoreViewer",
        ImmutableList.of("user:TheonStark.655151@gmail.com"));
    assertBoutContents(
        "Set HL7v2 store policy:",
        "roles/healthcare.hl7V2StoreViewer",
        "user:TheonStark.655151@gmail.com");
  }

  @Test
  public void test_02_ListHL7v2Stores() throws Exception {
    HL7v2List.listHL7v2Stores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertBoutContents("Retrieved \\d+ HL7v2 stores", hl7v2StoreName);
  }

  @Test
  public void test_02_PatchHL7v2Store() throws Exception {
    HL7v2Patch.patchHL7v2Store(hl7v2StoreName, GCLOUD_PUBSUB_TOPIC);
    assertBoutContents("Patched HL7v2 store:", hl7v2StoreName, GCLOUD_PUBSUB_TOPIC);
  }

  @Test
  public void test_02_CreateHL7v2Message() throws Exception {
    HL7v2MessageCreate.createHL7v2Message(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        hl7v2StoreId,
        "resources/hl7v2-sample-injest.txt");
    Pattern messageNamePattern =
        Pattern.compile(String.format("\"(?<messageName>%s/[^/\"]+)\"", hl7v2MessagePrefix));
    Matcher messageNameMatcher = messageNamePattern.matcher(bout.toString());
    assertThat(messageNameMatcher.find()).isTrue();
    // '=' is encoded for JSON, but won't work for 'get'.
    hl7v2MessageName = messageNameMatcher.group("messageName").replace("\\u003d", "=");
    hl7v2MessageId = hl7v2MessageName.substring(hl7v2MessageName.indexOf("messages/") + "messages/".length());
    assertBoutContents("Created HL7v2 message:", hl7v2StoreName);
  }

  @Test
  public void test_03_GetHL7v2Message() throws Exception {
    HL7v2MessageGet.getHL7v2Message(hl7v2MessageName);
    assertBoutContents("Retrieved HL7v2 message:", hl7v2MessageName.replace("=", ""));
  }

  @Test
  public void test_03_ListHL7v2Messages() throws Exception {
    HL7v2MessageList.listHL7v2Messages(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId, hl7v2StoreId);
    assertBoutContents("Retrieved \\d+ HL7v2 messages", hl7v2MessageName.replace("=", ""));
  }

  @Test
  public void test_03_PatchHL7v2Message() throws Exception {
    HL7v2MessagePatch.patchHL7v2Message(hl7v2MessageName, "labelKey", "labelValue");
    assertBoutContents(
        "Patched HL7v2 message:",
        hl7v2MessageName.replace("=", ""),
        "labelKey",
        "labelValue");
  }

  @Test
  public void test_04_IngestHL7v2Message() throws Exception {
    HL7v2MessageIngest.ingestHl7v2Message(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        hl7v2StoreId,
        hl7v2MessageName);
    assertBoutContents("Ingested HL7v2 message:", hl7v2MessageName.replace("=", ""));
  }

  @Test
  public void test_05_DeleteHL7v2Message() throws Exception {
    HL7v2MessageDelete.deleteHL7v2Message(hl7v2MessageName);
    HL7v2MessageList.listHL7v2Messages(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId, hl7v2StoreId);
    assertNotBoutContents(hl7v2MessageId);
  }

  @Test
  public void test_06_DeleteHL7v2Store() throws Exception {
    HL7v2Delete.deleteHL7v2Store(hl7v2StoreName);
    HL7v2List.listHL7v2Stores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertNotBoutContents(hl7v2StoreId);
  }
}
