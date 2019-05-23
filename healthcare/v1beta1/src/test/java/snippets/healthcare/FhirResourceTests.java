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
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
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
import snippets.healthcare.fhir.resources.FhirResourcePatch;
import snippets.healthcare.fhir.resources.FhirResourceSearch;
import snippets.healthcare.fhir.resources.FhirResourceSearchPost;

@RunWith(JUnit4.class)
public class FhirResourceTests extends TestBase {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String fhirStoreName;

  private String fhirResourceId;
  private String fhirResourceName;

  private static String patientType = "Patient";

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable \"%s\" is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @BeforeClass
  public static void setUp() throws IOException {
    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    String datasetName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        PROJECT_ID,
        REGION_ID,
        datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    String fhirStoreId = "fhir-" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirStoreName = String.format("%s/fhirStores/%s", datasetName, fhirStoreId);
    FhirStoreCreate.fhirStoreCreate(datasetName, fhirStoreId);
  }

  @AfterClass
  public static void deleteTempItems() throws IOException {
    deleteDatasets();
  }

  @Before
  public void beforeTest() throws IOException, URISyntaxException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    FhirResourceCreate.fhirResourceCreate(fhirStoreName, patientType);

    Matcher idMatcher = Pattern.compile("\"id\": \"([^\"]*)\",").matcher(bout.toString());
    if (idMatcher.find()) {
      fhirResourceId = idMatcher.group(1);
      fhirResourceName = String.format("%s/fhir/%s/%s", fhirStoreName, patientType, fhirResourceId);
    }

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }


  @Test
  public void test_FhirResourceCreate() throws Exception {
    FhirResourceCreate.fhirResourceCreate(fhirStoreName, patientType);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource created:"));
  }

  @Test
  public void test_FhirResourceSearch() throws Exception {
    FhirResourceSearch.fhirResourceSearch(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_FhirResourceSearchPost() throws Exception {
    FhirResourceSearchPost.fhirResourceSearchPost(fhirStoreName, patientType);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_FhirResourceGet() throws Exception {
    FhirResourceGet.fhirResourceGet(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource retrieved:"));
  }

  @Test
  public void test_FhirResourcePatch() throws Exception {
    FhirResourcePatch.fhirResourcePatch(
        fhirResourceName,
        "[{\"op\": \"add\", \"path\": \"/active\", \"value\": false}]");

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource patched:"));
  }

  @Test
  public void test_FhirResourceConditionalPatch() throws Exception {
    FhirResourceConditionalPatch.fhirResourceConditionalPatch(
        fhirStoreName,
        patientType,
        fhirResourceId,
        "[{\"op\": \"add\", \"path\": \"/active\", \"value\": true}]");

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource conditionally patched:"));
  }

  @Test
  public void test_FhirResourceConditionalUpdate() throws Exception {
    FhirResourceConditionalUpdate.fhirResourceConditionalUpdate(
        fhirStoreName,
        patientType,
        String.format(
            "{\"resourceType\": \"%s\", \"active\": true, \"id\": \"%s\"}",
            patientType,
            fhirResourceId));

    String output = bout.toString();
    assertThat(output, containsString(String.format("FHIR resource conditionally replaced:")));
  }

  @Test
  public void test_FhirResourceGetPatientEverything() throws Exception {
    FhirResourceGetPatientEverything.fhirResourceGetPatientEverything(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  public void test_GetFhirResourceMetadata() throws Exception {
    FhirResourceGetMetadata.fhirResourceGetMetadata(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource metadata retrieved:"));
  }

  @Test
  public void test_FhirResourceDelete() throws Exception {
    FhirResourceDelete.fhirResourceDelete(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource deleted."));

    bout.reset();
    try {
      FhirResourceGet.fhirResourceGet(fhirResourceName);
      fail();
    } catch (RuntimeException ex) {
      assertThat(ex.getMessage(), containsString("404"));
    }
  }

  @Test
  public void test_FhirResourceGetHistory() throws Exception {
    FhirResourcePatch.fhirResourcePatch(
        fhirResourceName,
        "[{\"op\": \"add\", \"path\": \"/active\", \"value\": false}]");
    FhirResourceGetHistory.fhirResourceGetHistory(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource history retrieved:"));
  }

  @Test
  public void test_DeletePurgeFhirResource() throws Exception {
    FhirResourcePatch.fhirResourcePatch(
        fhirResourceName,
        "[{\"op\": \"add\", \"path\": \"/active\", \"value\": false}]");
    FhirResourceDelete.fhirResourceDelete(fhirResourceName);
    FhirResourceDeletePurge.fhirResourceDeletePurge(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource purged."));
  }
}
