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
import snippets.healthcare.dicom.DicomStoreCreate;
import snippets.healthcare.dicom.DicomStoreDelete;
import snippets.healthcare.dicom.DicomStoreExport;
import snippets.healthcare.dicom.DicomStoreGet;
import snippets.healthcare.dicom.DicomStoreGetIamPolicy;
import snippets.healthcare.dicom.DicomStoreImport;
import snippets.healthcare.dicom.DicomStoreList;
import snippets.healthcare.dicom.DicomStorePatch;
import snippets.healthcare.dicom.DicomStoreSetIamPolicy;

@RunWith(JUnit4.class)
public class DicomStoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = "java-docs-samples-testing";
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String datasetName;

  private static String dicomStoreName;

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

    String dicomStoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    dicomStoreName = String.format("%s/dicomStores/%s", datasetName, dicomStoreId);

    DicomStoreCreate.dicomStoreCreate(datasetName, dicomStoreId);

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void test_DicomStoreCreate() throws IOException {
    DicomStoreCreate.dicomStoreCreate(datasetName, "new-dicom-store");

    String output = bout.toString();
    assertThat(output, containsString("DICOM store created:"));
  }

  @Test
  public void test_DicomStoreGet() throws IOException {
    DicomStoreGet.dicomeStoreGet(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store retrieved:"));
  }

  @Test
  public void test_DicomStoreGetIamPolicy() throws IOException {
    DicomStoreGetIamPolicy.dicomStoreGetIamPolicy(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store IAMPolicy retrieved:"));
  }

  @Test
  public void test_DicomStoreSetIamPolicy() throws IOException {
    DicomStoreSetIamPolicy.dicomStoreSetIamPolicy(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM policy has been updated: "));
  }

  @Test
  public void test_DicomStoreList() throws IOException {
    DicomStoreList.dicomStoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved"));
  }

  @Test
  public void test_DicomStorePatch() throws IOException {
    DicomStorePatch.patchDicomStore(dicomStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store patched: "));
  }

  @Test
  public void test_DicomStoreExport() throws IOException {
    String gcsPath = String.format("gs://%s", GCLOUD_BUCKET_NAME);
    DicomStoreExport.dicomStoreExport(dicomStoreName, gcsPath);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store export complete."));
  }

  @Test
  public void test_DicomStoreImport() throws IOException {
    String gcsPath =
        String.format("gs://%s/%s/%s", GCLOUD_BUCKET_NAME, "healthcare-api", "000009.dcm");
    DicomStoreImport.dicomStoreImport(dicomStoreName, gcsPath);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store import complete."));
  }

  @Test
  public void test_DicomStoreDelete() throws IOException {
    DicomStoreDelete.deleteDicomStore(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store deleted."));
  }
}
