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
import snippets.healthcare.dicom.DicomStoreCreate;
import snippets.healthcare.dicom.DicomStoreDelete;
import snippets.healthcare.dicom.DicomStoreExport;
import snippets.healthcare.dicom.DicomStoreGet;
import snippets.healthcare.dicom.DicomStoreGetIamPolicy;
import snippets.healthcare.dicom.DicomStoreImport;
import snippets.healthcare.dicom.DicomStoreList;
import snippets.healthcare.dicom.DicomStorePatch;
import snippets.healthcare.dicom.DicomStoreSetIamPolicy;
import snippets.healthcare.dicom.DicomWebDeleteStudy;
import snippets.healthcare.dicom.DicomWebRetrieveStudy;
import snippets.healthcare.dicom.DicomWebSearchForInstances;
import snippets.healthcare.dicom.DicomWebStoreInstance;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DicomStoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = System.getenv("GCLOUD_BUCKET_NAME");
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String datasetId;
  private static String datasetName;

  private static String dicomStoreId;
  private static String dicomStoreName;

  private static String studyId;

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

    dicomStoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    dicomStoreName = String.format("%s/dicomStores/%s", datasetName, dicomStoreId);

    studyId = "study-" + UUID.randomUUID().toString().replaceAll("-", "_");
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
  public void test_01_DicomStoreCreate() throws IOException {
    DicomStoreCreate.dicomStoreCreate(datasetName, dicomStoreId);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store created:"));
  }

  @Test
  public void test_02_DicomStoreGet() throws IOException {
    DicomStoreGet.dicomeStoreGet(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store retrieved:"));
  }

  @Test
  public void test_02_DicomStoreGetIamPolicy() throws IOException {
    DicomStoreGetIamPolicy.dicomStoreGetIamPolicy(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store IAMPolicy retrieved:"));
  }

  @Test
  public void test_02_DicomStoreSetIamPolicy() throws IOException {
    DicomStoreSetIamPolicy.dicomStoreSetIamPolicy(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM policy has been updated: "));
  }

  @Test
  public void test_02_DicomStoreList() throws IOException {
    DicomStoreList.dicomStoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved"));
  }

  @Test
  public void test_02_DicomStorePatch() throws IOException {
    DicomStorePatch.patchDicomStore(dicomStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store patched: "));
  }

  @Test
  public void test_02_DicomStoreExport() throws IOException {
    DicomStoreExport.dicomStoreExport(dicomStoreName, GCLOUD_BUCKET_NAME);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store export complete."));
  }

  @Test
  public void test_02_DicomStoreImport() throws IOException {
    String gcsPath =
        String.format("gcs://%s/%s", GCLOUD_BUCKET_NAME, "IM-0002-0001-JPEG-BASELINE.dcm");
    DicomStoreImport.dicomStoreImport(dicomStoreName, gcsPath);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store import complete."));
  }

  @Test
  public void test_03_DicomWebStoreInstance() throws Exception {
    DicomWebStoreInstance.dicomWebStoreInstance(
        dicomStoreName, studyId, "src/test/resources/dicom_00000001_000.dcm");

    String output = bout.toString();
    assertThat(output, containsString("DICOM instance stored:"));
  }

  @Test
  public void test_04_DicomWebSearchInstances() throws Exception {
    DicomWebSearchForInstances.dicomWebSearchForInstances(dicomStoreName);
    String output = bout.toString();
    assertThat(output, containsString("DICOM instances found:"));
  }

  @Test
  public void test_04_DicomWebRetrieveStudy() throws Exception {
    DicomWebRetrieveStudy.dicomWebRetrieveStudy(dicomStoreName, studyId);

    String output = bout.toString();
    assertThat(output, containsString("DICOM study retrieved:"));
  }

  @Test
  public void test_05_DicomWebDeleteStudy() throws IOException {
    DicomWebDeleteStudy.dicomWebDeleteStudy(dicomStoreName, studyId);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store study deleted."));
  }

  @Test
  public void test_06_DicomStoreDelete() throws IOException {
    DicomStoreDelete.deleteDicomStore(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store deleted."));
  }
}
