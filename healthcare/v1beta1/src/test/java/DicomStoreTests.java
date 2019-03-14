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
import com.google.healthcare.dicom.DicomStoreCreate;
import com.google.healthcare.dicom.DicomStoreDelete;
import com.google.healthcare.dicom.DicomStoreExport;
import com.google.healthcare.dicom.DicomStoreGet;
import com.google.healthcare.dicom.DicomStoreGetIamPolicy;
import com.google.healthcare.dicom.DicomStoreImport;
import com.google.healthcare.dicom.DicomStoreList;
import com.google.healthcare.dicom.DicomStorePatch;
import com.google.healthcare.dicom.DicomStoreSetIamPolicy;
import com.google.healthcare.dicom.web.DicomWebDeleteStudy;
import com.google.healthcare.dicom.web.DicomWebRetrieveStudy;
import com.google.healthcare.dicom.web.DicomWebSearchForInstances;
import com.google.healthcare.dicom.web.DicomWebStoreInstance;

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
public class DicomStoreTests extends HealthcareTestBase {
  private static String dicomStoreId;
  private static String dicomStoreName;
  private static String studyId;
  private static String storageFileName = "IM-0002-0001-JPEG-BASELINE.dcm";
  private static String gcsFileName = GCLOUD_BUCKET_NAME + "/" + storageFileName;

  @BeforeClass
  public static void setUp() throws IOException {
    setUpClass();

    dicomStoreId = "dicom-store-" + suffix;

    dicomStoreName = String.format(
        "projects/%s/locations/%s/datasets/%s/dicomStores/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        dicomStoreId);

    studyId = "study-" + suffix;

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
  public void test_01_CreateDicomStore() throws Exception {
    DicomStoreCreate.createDicomStore(
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId,
        dicomStoreId);
    assertBoutContents("Created Dicom store:", dicomStoreName);
  }

  @Test
  public void test_02_GetDicomStore() throws Exception {
    DicomStoreGet.getDicomStore(dicomStoreName);
    assertBoutContents("Retrieved Dicom store:", dicomStoreName);
  }

  @Test
  public void test_02_GetDicomStoreIamPolicy() throws Exception {
    DicomStoreGetIamPolicy.getIamPolicy(dicomStoreName);
    assertBoutContents("Retrieved Dicom store policy:.*etag.*ACAB");
  }

  @Test
  public void test_02_SetDicomStoreIamPolicy() throws Exception {
    DicomStoreSetIamPolicy.setIamPolicy(
        dicomStoreName,
        "roles/healthcare.dicomStoreViewer",
        ImmutableList.of("user:TheonStark.655151@gmail.com"));
    assertBoutContents(
        "Set Dicom store policy:",
        "roles/healthcare.dicomStoreViewer",
        "user:TheonStark.655151@gmail.com");
  }

  @Test
  public void test_02_ListDicomStores() throws Exception {
    DicomStoreList.listDicomStores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertBoutContents("Retrieved \\d+ Dicom stores", dicomStoreName);
  }

  @Test
  public void test_02_PatchDicomStore() throws Exception {
    DicomStorePatch.patchDicomStore(dicomStoreName, GCLOUD_PUBSUB_TOPIC);
    assertBoutContents("Patched Dicom store:", dicomStoreName, GCLOUD_PUBSUB_TOPIC);
  }

  @Test
  public void test_02_ExportDicomStore() throws Exception {
    DicomStoreExport.exportDicomStoreInstance(dicomStoreName, GCLOUD_BUCKET_NAME);
    assertBoutContents("Exporting Dicom store op name:");
  }

  @Test
  public void test_02_ImportDicomStore() throws Exception {
    DicomStoreImport.importDicomStoreInstance(dicomStoreName, gcsFileName);
    assertBoutContents("Importing Dicom store op name:");
  }

  @Test
  @Ignore // TODO: b/128024001
  public void test_03_DicomWebStoreInstance() throws Exception {
    DicomWebStoreInstance.storeInstance(dicomStoreName, studyId);
    assertBoutContents("Stored Dicom store:", studyId);
  }

  @Test
  @Ignore // TODO: b/128024001
  public void test_04_DicomWebSearchInstances() throws Exception {
    DicomWebSearchForInstances.searchForInstances(dicomStoreName);
    assertBoutContents("Found Dicom store instances:", studyId);
  }

  @Test
  @Ignore // TODO: b/128024001
  public void test_04_DicomWebRetrieveStudy() throws Exception {
    DicomWebRetrieveStudy.retrieveStudy(dicomStoreName, studyId);
    assertBoutContents("Retrieved Dicom study:", studyId);
  }

  @Test
  @Ignore // TODO: b/128024001
  public void test_05_DicomWebDeleteStudy() throws Exception {
    DicomWebDeleteStudy.deleteStudy(dicomStoreName, studyId);
    DicomWebSearchForInstances.searchForInstances(dicomStoreName);
    assertNotBoutContents(studyId);
  }

  @Test
  public void test_06_DeleteDicomStore() throws Exception {
    DicomStoreDelete.deleteDicomStore(dicomStoreName);

    // Verify delete successful.
    DicomStoreList.listDicomStores(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertNotBoutContents(dicomStoreId);
  }
}
