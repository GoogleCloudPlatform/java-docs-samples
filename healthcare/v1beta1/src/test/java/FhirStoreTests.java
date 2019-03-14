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

import com.google.common.collect.ImmutableList;
import com.google.healthcare.datasets.DatasetCreate;
import com.google.healthcare.fhir.FhirStoreCreate;
import com.google.healthcare.fhir.FhirStoreDelete;
import com.google.healthcare.fhir.FhirStoreExecuteBundle;
import com.google.healthcare.fhir.FhirStoreExport;
import com.google.healthcare.fhir.FhirStoreGet;
import com.google.healthcare.fhir.FhirStoreGetIamPolicy;
import com.google.healthcare.fhir.FhirStoreImport;
import com.google.healthcare.fhir.FhirStoreList;
import com.google.healthcare.fhir.FhirStorePatch;
import com.google.healthcare.fhir.FhirStoreSetIamPolicy;
import com.google.healthcare.fhir.resources.FhirResourceConditionalDelete;
import com.google.healthcare.fhir.resources.FhirResourceConditionalPatch;
import com.google.healthcare.fhir.resources.FhirResourceConditionalUpdate;
import com.google.healthcare.fhir.resources.FhirResourceCreate;
import com.google.healthcare.fhir.resources.FhirResourceDelete;
import com.google.healthcare.fhir.resources.FhirResourceGet;
import com.google.healthcare.fhir.resources.FhirResourceGetMetadata;
import com.google.healthcare.fhir.resources.FhirResourceGetPatientEverything;
import com.google.healthcare.fhir.resources.FhirResourcePatch;
import com.google.healthcare.fhir.resources.FhirResourceSearch;
import com.google.healthcare.fhir.resources.FhirResourceSearchPost;
import com.google.healthcare.fhir.resources.history.FhirResourceDeletePurge;
import com.google.healthcare.fhir.resources.history.FhirResourceGetHistory;
import com.google.healthcare.fhir.resources.history.FhirResourceListHistory;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FhirStoreTests extends HealthcareTestBase {
  private static String storageFileName = "IM-0002-0001-JPEG-BASELINE.dcm";
  private static String gcsFileName = GCLOUD_BUCKET_NAME + "/" + storageFileName;
  private static String fhirStoreId;
  private static String fhirStoreName;
  private static String fhirResourceType = "Patient";
  private static String fhirResourceType2 = "Observation";
  private static String fhirResourceName;
  private static String fhirResourceName2;

  @BeforeClass
  public static void setUp() throws IOException {
    setUpClass();

    fhirStoreId = "fhir-store-" + suffix;

    fhirStoreName = String.format(
        "projects/%s/locations/%s/datasets/%s/fhirStores/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId);

    fhirResourceName = String.format(
        "projects/%s/locations/%s/datasets/%s/fhirStores/%s/fhir/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType);

    fhirResourceName2 = String.format(
        "projects/%s/locations/%s/datasets/%s/fhirStores/%s/fhir/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType2);

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
  public void test_01_CreateFhirStore() throws Exception {
    FhirStoreCreate.createFhirStore(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId);
    assertBoutContents("Created FHIR store:", fhirStoreId);
  }

  @Test
  public void test_02_GetFhirStore() throws Exception {
    FhirStoreGet.getFhirStore(fhirStoreName);
    assertBoutContents("Retrieved FHIR store:", fhirStoreName);
  }

  @Test
  public void test_02_GetFhirIamPolicy() throws Exception {
    FhirStoreGetIamPolicy.getIamPolicy(fhirStoreName);
    assertBoutContents("FHIR store IAM policy retrieved:.*etag.*ACAB");
  }

  @Test
  public void test_02_SetFhirIamPolicy() throws Exception {
    FhirStoreSetIamPolicy.setIamPolicy(
        fhirStoreName,
        "roles/healthcare.fhirStoreViewer",
        ImmutableList.of("user:TheonStark.655151@gmail.com"));
    assertBoutContents(
        "Set FHIR store IAM policy:",
        "roles/healthcare.fhirStoreViewer",
        "user:TheonStark.655151@gmail.com");
  }

  @Test
  public void test_02_ListFhirStores() throws Exception {
    FhirStoreList.listFhirStores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertBoutContents("Retrieved \\d+ FHIR stores", fhirStoreName);
  }

  @Test
  public void test_02_PatchFhirStore() throws Exception {
    FhirStorePatch.patchFhirStore(fhirStoreName, GCLOUD_PUBSUB_TOPIC);
    assertBoutContents("Patched FHIR store:", fhirStoreName, GCLOUD_PUBSUB_TOPIC);
  }

  @Test
  public void test_02_ExportFhirStore() throws Exception {
    FhirStoreExport.exportFhirStore(fhirStoreName, gcsFileName);
    assertBoutContents("Exporting FHIR store operation name:");
  }

  @Test
  public void test_03_ImportFhirStore() throws Exception {
    FhirStoreImport.importFhirStore(fhirStoreName, gcsFileName);
    assertBoutContents("Importing FHIR store operation name:");
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_02_ExecuteFhirBundle() throws Exception {
    FhirStoreExecuteBundle.executeBundle(fhirStoreName);
    assertBoutContents("Executed FHIR bundle:", datasetId);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_03_CreateFhirResource() throws Exception {
    FhirResourceCreate.createFhirResource(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType);
    assertBoutContents("Created FHIR resource:");
    // TODO: Get Resource ID for getPatientEverything test.
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_SearchFhirResources() throws Exception {
    FhirResourceSearch.searchFhirResources(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType);
    assertBoutContents("FHIR resource search results:", fhirResourceName);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_SearchFhirResourcesPost() throws Exception {
    FhirResourceSearchPost.searchFhirResourcesPost(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType);
    assertBoutContents("FHIR resource search results:", fhirResourceName);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_GetFhirResource() throws Exception {
    FhirResourceGet.getFhirResource(fhirResourceName);
    assertBoutContents("Retrieved FHIR resource:", fhirResourceName);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_PatchFhirResource() throws Exception {
    FhirResourcePatch.patchFhirResource(fhirResourceName, "{'family': 'Jones'}");
    assertBoutContents("Patched FHIR resource:", fhirResourceName, "Jones");
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_ConditionalPatchFhirResource() throws Exception {
    FhirResourceConditionalPatch.conditionalPatchFhirResource(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceName,
        fhirResourceType,
        "{'family': 'Smith'}");
    assertBoutContents("Conditionally patched FHIR resource:", fhirResourceName, "Smith");
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_ConditionalUpdateFhirResource() throws Exception {
    FhirResourceConditionalUpdate.conditionalUpdateFhirResource(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceName,
        fhirResourceType,
        "{'family': 'Washington'}");
    assertBoutContents("Conditionally updated FHIR resource:", fhirResourceName, "Washington");
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_ConditionalDeleteFhirResource() throws Exception {
    FhirResourceCreate.createFhirResource(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType2);
    FhirResourceConditionalDelete.conditionalDeleteFhirResource(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        fhirStoreId,
        fhirResourceType2);
    assertBoutContents("Conditionally deleted FHIR resource:", fhirResourceName2);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_04_GetPatientEverything() throws Exception {
    FhirResourceGetPatientEverything.getPatientEverything(fhirResourceName);
    assertBoutContents("FHIR resource search results:", fhirResourceName + "/[parse-from-CreateResource]");
  }


  @Test
  @Ignore // TODO: b/128844810
  public void test_04_GetFhirResourceMetadata() throws Exception {
    FhirResourceGetMetadata.getMetadata(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId, fhirStoreId);
    assertBoutContents("FHIR resource metadata retrieved:");
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_05_DeleteFhirResource() throws Exception {
    FhirResourceDelete.deleteFhirResource(fhirResourceName);
    FhirResourceGet.getFhirResource(fhirResourceName);
    assertNotBoutContents(fhirResourceType);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_06_GetFhirResourceHistory() throws Exception {
    // Even after delete, get & list resource history will return previous versions of resources.
    FhirResourceGetHistory.getFhirResourceHistory(fhirResourceName);
    assertBoutContents("Retrieved FHIR resource history:", fhirResourceName);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_06_ListFhirResourceHistory() throws Exception {
    // Even after delete, get & list resource history will return previous versions of resources.
    FhirResourceListHistory.listFhirResourceHistory(fhirResourceName);
    assertBoutContents("Listed FHIR resource history:", fhirResourceName);
  }

  @Test
  @Ignore // TODO: b/128844810
  public void test_07_DeletePurgeFhirResource() throws Exception {
    //Delete+Purge will remove a resource's version history.
    FhirResourceDeletePurge.deletePurgeFhirResource(fhirResourceName);

    FhirResourceGetHistory.getFhirResourceHistory(fhirResourceName);
    assertNotBoutContents(fhirResourceType);
  }

  @Test
  public void test_08_DeleteFhirStore() throws Exception {
    FhirStoreDelete.deleteFhirStore(fhirStoreName);

    // Verify delete successful.
    FhirStoreList.listFhirStores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertNotBoutContents(fhirStoreId);
  }
}
