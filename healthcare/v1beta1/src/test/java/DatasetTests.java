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
import com.google.healthcare.datasets.DatasetDeIdentify;
import com.google.healthcare.datasets.DatasetDelete;
import com.google.healthcare.datasets.DatasetGet;
import com.google.healthcare.datasets.DatasetGetIamPolicy;
import com.google.healthcare.datasets.DatasetList;
import com.google.healthcare.datasets.DatasetPatch;
import com.google.healthcare.datasets.DatasetSetIamPolicy;

import java.io.IOException;
import java.util.TimeZone;
import java.util.UUID;

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
public class DatasetTests extends HealthcareTestBase {
  private static String datasetName;

  @BeforeClass
  public static void setUp() {
    setUpClass();

    datasetName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        datasetId);
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
  public void test_01_CreateDataset() throws Exception {
    DatasetCreate.createDataset(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION, datasetId);
    assertBoutContents("Created Dataset: .+", datasetId);
  }

  @Test
  public void test_02_GetDataset() throws Exception {
    DatasetGet.getDataset(datasetName);
    assertBoutContents("Retrieved Dataset: .+", datasetName);
  }

  @Test
  public void test_02_ListDataset() throws Exception {
    DatasetList.listDatasets(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION);
    assertBoutContents("Retrieved \\d+ datasets", datasetName);
  }

  @Test
  public void test_02_PatchDataset() throws IOException {
    String newTimeZone = TimeZone.getAvailableIDs()[0];
    DatasetPatch.patchDataset(datasetName, newTimeZone);
    assertBoutContents("Patched Dataset", datasetName, newTimeZone);
  }

  @Test
  public void test_02_DeidDataset() throws Exception {
    String newDatasetId = "dataset-" + UUID
        .randomUUID()
        .toString()
        .substring(0, 4)
        .replaceAll("-", "_");
    String newDatasetName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION,
        newDatasetId);

    DatasetDeIdentify.deidentifyDataset(datasetName, newDatasetName);
    assertBoutContents("Deidentified Dataset: ", newDatasetName);

    DatasetDelete.deleteDataset(newDatasetName);
    DatasetList.listDatasets(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION);
    assertNotBoutContents(newDatasetId);
  }

  @Test
  public void test_02_GetDatasetIamPolicy() throws Exception {
    DatasetGetIamPolicy.getIamPolicy(datasetName);
    assertBoutContents("Retrieved Dataset policy:.*etag.*", "ACAB");
  }

  @Test
  public void test_02_SetDatasetIamPolicy() throws Exception {
    DatasetSetIamPolicy.setIamPolicy(
        datasetName,
        "roles/healthcare.datasetViewer",
        ImmutableList.of("user:TheonStark.655151@gmail.com"));
    assertBoutContents(
        "Set Dataset store policy:",
        "roles/healthcare.datasetViewer",
        "user:TheonStark.655151@gmail.com");
  }

  @Test
  public void test_03_DeleteDataset() throws Exception {
    // Test this last because we use the Dataset for other data type tests.
    DatasetDelete.deleteDataset(datasetName);

    // Verify delete successful.
    DatasetList.listDatasets(DEFAULT_PROJECT_ID, DEFAULT_CLOUD_REGION);
    assertNotBoutContents(datasetId);
  }
}
