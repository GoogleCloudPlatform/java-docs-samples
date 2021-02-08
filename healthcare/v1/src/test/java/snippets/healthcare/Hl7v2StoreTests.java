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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
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
import snippets.healthcare.hl7v2.Hl7v2StoreDelete;
import snippets.healthcare.hl7v2.Hl7v2StoreGet;
import snippets.healthcare.hl7v2.Hl7v2StoreGetIamPolicy;
import snippets.healthcare.hl7v2.Hl7v2StoreList;
import snippets.healthcare.hl7v2.Hl7v2StoreSetIamPolicy;

@RunWith(JUnit4.class)
public class Hl7v2StoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String datasetName;

  private static String hl7v2StoreName;

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

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void test_Hl7v2StoreCreate() throws Exception {
    Hl7v2StoreCreate.hl7v2StoreCreate(datasetName, "new-hlv2store");

    String output = bout.toString();
    assertThat(output, containsString("Hl7V2Store store created:"));
  }

  @Test
  public void test_Hl7v2StoreGet() throws Exception {
    Hl7v2StoreGet.hl7v2eStoreGet(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 store retrieved:"));
  }

  @Test
  public void test_Hl7v2StoreGetIamPolicy() throws Exception {
    Hl7v2StoreGetIamPolicy.hl7v2StoreGetIamPolicy(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 store IAMPolicy retrieved:"));
  }

  @Test
  public void test_Hl7v2StoreSetIamPolicy() throws Exception {
    Hl7v2StoreSetIamPolicy.hl7v2StoreSetIamPolicy(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 policy has been updated:"));
  }

  @Test
  public void test_Hl7v2StoreList() throws Exception {
    Hl7v2StoreList.hl7v2StoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved"));
  }

  @Test
  public void test_Hl7v2StoreDelete() throws Exception {
    Hl7v2StoreDelete.hl7v2StoreDelete(hl7v2StoreName);

    String output = bout.toString();
    assertThat(output, containsString("HL7v2 store deleted."));
  }
}
