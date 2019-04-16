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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import snippets.healthcare.datasets.DatasetCreate;
import snippets.healthcare.fhir.FhirStoreCreate;
import snippets.healthcare.fhir.resources.FhirResourceConditionalPatch;
import snippets.healthcare.fhir.resources.FhirResourceConditionalUpdate;
import snippets.healthcare.fhir.resources.FhirResourceCreate;
import snippets.healthcare.fhir.resources.FhirResourceDelete;
import snippets.healthcare.fhir.resources.FhirResourceDeletePurge;
import snippets.healthcare.fhir.resources.FhirResourceGet;
import snippets.healthcare.fhir.resources.FhirResourceGetHistory;
import snippets.healthcare.fhir.resources.FhirResourceGetMetadata;
import snippets.healthcare.fhir.resources.FhirResourceGetPatientEverything;
import snippets.healthcare.fhir.resources.FhirResourceListHistory;
import snippets.healthcare.fhir.resources.FhirResourcePatch;
import snippets.healthcare.fhir.resources.FhirResourceSearch;
import snippets.healthcare.fhir.resources.FhirResourceSearchPost;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FhirResourceTests extends TestBase {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = "java-docs-samples-testing";
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String storageFileName = "IM-0002-0001-JPEG-BASELINE.dcm";
  private static String gcsFileName = GCLOUD_BUCKET_NAME + "/" + storageFileName;

  private static String datasetId;
  private static String datasetName;

  private static String fhirStoreId;
  private static String fhirStoreName;

  private static String fhirResourceId;
  private static String fhirResourceName;

  private static String patientType = "Patient";

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
    datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    datasetName =
        String.format("projects/%s/locations/%s/datasets/%s", PROJECT_ID, REGION_ID, datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    fhirStoreId = "fhir-" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirStoreName = String.format("%s/fhirStores/%s", datasetName, fhirStoreId);
    FhirStoreCreate.fhirStoreCreate(datasetName, fhirStoreId);

    fhirResourceId = patientType + "/" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirResourceName = String.format("%s/fhir/%s", fhirStoreName, fhirResourceId);
  }

  @AfterClass
  public static void deleteTempItems() throws IOException {
    deleteDatasets();

  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }


  @Test
  public void test_01_FhirResourceCreate() throws Exception {
    FhirResourceCreate.fhirResourceCreate(fhirStoreName, patientType);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource created:"));
  }

  @Test
  public void test_02_FhirResourceSearch() throws Exception {
    FhirResourceSearch.fhirResourceSearch(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_02_FhirResourceSearchPost() throws Exception {
    FhirResourceSearchPost.fhirResourceSearchPost(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_02_FhirResourceGet() throws Exception {
    FhirResourceGet.fhirResourceGet(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource retrieved:"));
  }

  @Test
  public void test_02_FhirResourcePatch() throws Exception {
    FhirResourcePatch.fhirResourcePatch(fhirResourceName, "[{op: 'replace', path: '/active', value: false}]");

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource patched:"));
    assertThat(output, containsString("Matthews"));
  }

  @Test
  public void test_02_FhirResourceConditionalPatch() throws Exception {
    FhirResourceConditionalPatch.fhirResourceConditionalPatch(
        fhirStoreName,
        patientType,
        "{'family': 'Smith'}");

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource conditionally patched:"));
  }

  @Test
  public void test_02_FhirResourceConditionalUpdate() throws Exception {
    FhirResourceConditionalUpdate.fhirResourceConditionalUpdate(
        fhirStoreName,
        patientType,
        "{'family': 'Jones'}");

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource conditionally replaced:"));
  }

  @Test
  public void test_02_FhirResourceGetPatientEverything() throws Exception {
    FhirResourceGetPatientEverything.fhirResourceGetPatientEverything(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_02_GetFhirResourceMetadata() throws Exception {
    FhirResourceGetMetadata.fhirResourceGetMetadata(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource metadata retrieved:"));
  }

  @Test
  public void test_03_FhirResourceDelete() throws Exception {
    FhirResourceDelete.fhirResourceDelete(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource deleted."));
  }

  @Test
  public void test_04_FhirResourceGetHistory() throws Exception {
    FhirResourceGetHistory.fhirResourceGetHistory(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource history retrieved:"));
  }

  @Test
  public void test_04_FhirResourceListHistory() throws Exception {
    FhirResourceListHistory.fhirResourceListHistory(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource history list:"));
  }

  @Test
  public void test_05_DeletePurgeFhirResource() throws Exception {
    FhirResourceDeletePurge.fhirResourceDeletePurge(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource deleted."));
  }
}
