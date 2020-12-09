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
import snippets.healthcare.fhir.FhirStoreCreate;
import snippets.healthcare.fhir.FhirStoreDelete;
import snippets.healthcare.fhir.FhirStoreExecuteBundle;
import snippets.healthcare.fhir.FhirStoreExport;
import snippets.healthcare.fhir.FhirStoreGet;
import snippets.healthcare.fhir.FhirStoreGetIamPolicy;
import snippets.healthcare.fhir.FhirStoreGetMetadata;
import snippets.healthcare.fhir.FhirStoreImport;
import snippets.healthcare.fhir.FhirStoreList;
import snippets.healthcare.fhir.FhirStorePatch;
import snippets.healthcare.fhir.FhirStoreSetIamPolicy;

@RunWith(JUnit4.class)
public class FhirStoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = "java-docs-samples-testing";
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String datasetName;

  private static String fhirStoreName;

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
    requireEnvVar("GCLOUD_PUBSUB_TOPIC");
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

    String fhirStoreId = "fhir-" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirStoreName = String.format("%s/fhirStores/%s", datasetName, fhirStoreId);

    FhirStoreCreate.fhirStoreCreate(datasetName, fhirStoreId);

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void test_FhirStoreCreate() throws IOException {
    FhirStoreCreate.fhirStoreCreate(datasetName, "new-fhir-store");

    String output = bout.toString();
    assertThat(output, containsString("FHIR store created: "));
  }

  @Test
  public void test_FhirStoreGet() throws Exception {
    FhirStoreGet.fhirStoreGet(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store retrieved:"));
  }

  @Test
  public void test_FhirStoreGetMetadata() throws Exception {
    FhirStoreGetMetadata.fhirStoreGetMetadata(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store metadata retrieved:"));
  }

  @Test
  public void test_FhirStoreGetIamPolicy() throws Exception {
    FhirStoreGetIamPolicy.fhirStoreGetIamPolicy(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store IAMPolicy retrieved:"));
  }

  @Test
  public void test_FhirStoreSetIamPolicy() throws Exception {
    FhirStoreSetIamPolicy.fhirStoreSetIamPolicy(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR policy has been updated:"));
  }

  @Test
  public void test_FhirStoreList() throws Exception {
    FhirStoreList.fhirStoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved "));
  }

  @Test
  public void test_FhirStorePatch() throws Exception {
    FhirStorePatch.fhirStorePatch(fhirStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("Fhir store patched:"));
  }

  @Test
  public void test_ExecuteFhirBundle() throws Exception {
    FhirStoreExecuteBundle.fhirStoreExecuteBundle(
        fhirStoreName,
        "{\"resourceType\": \"Bundle\",\"type\": \"batch\",\"entry\": []}");

    String output = bout.toString();
    assertThat(output, containsString("FHIR bundle executed:"));
  }

  @Test
  public void test_FhirStoreExport() throws Exception {
    FhirStoreExport.fhirStoreExport(fhirStoreName, "gs://" + GCLOUD_BUCKET_NAME);

    String output = bout.toString();
    assertThat(output, containsString("Fhir store export complete."));
  }

  @Test
  public void test_FhirStoreImport() throws Exception {
    FhirStoreImport.fhirStoreImport(
        fhirStoreName,
        "gs://" + GCLOUD_BUCKET_NAME + "/healthcare-api/Patient.json");

    String output = bout.toString();
    assertThat(output, containsString("FHIR store import complete:"));
  }

  @Test
  public void test_FhirStoreDelete() throws Exception {
    FhirStoreDelete.fhirStoreDelete(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store deleted."));
  }
}
