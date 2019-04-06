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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import snippets.healthcare.datasets.DatasetCreate;
import snippets.healthcare.datasets.DatasetDeIdentify;
import snippets.healthcare.datasets.DatasetDelete;
import snippets.healthcare.datasets.DatasetGet;
import snippets.healthcare.datasets.DatasetGetIamPolicy;
import snippets.healthcare.datasets.DatasetList;
import snippets.healthcare.datasets.DatasetPatch;
import snippets.healthcare.datasets.DatasetSetIamPolicy;

@RunWith(JUnit4.class)
public class DatasetTests {
  private static final String DATASET_NAME = "projects/%s/locations/%s/datasets/%s";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

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

  private void testDatasetCreate(String datasetId) throws IOException {
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    String output = bout.toString();
    assertThat(output, containsString("Dataset created."));
  }

  private void testDatasetGet(String datasetName) throws IOException {
    DatasetGet.datasetGet(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset retrieved:"));
  }

  private void testDatasetList() throws IOException {
    DatasetList.datasetList(PROJECT_ID, REGION_ID);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved \\d+ datasets:"));
  }

  private void testDatasetGetIamPolicy(String datasetName) throws IOException {
    DatasetGetIamPolicy.datasetGetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset IAMPolicy retrieved:"));
  }

  private void testDatasetSetIamPolicy(String datasetName) throws IOException {
    DatasetSetIamPolicy.datasetSetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset policy has been updated: "));
  }

  private void testDatasetDeidentify(String srcDatasetName, String destDatasetName)
      throws IOException {
    DatasetDeIdentify.datasetDeIdentify(srcDatasetName, destDatasetName);

    String output = bout.toString();
    assertThat(output, containsString("De-identified dataset created."));
  }

  private void testDataSetPatch(String datasetName) throws IOException {
    DatasetPatch.datasetPatch(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset policy has been updated: "));
  }

  private void testDatasetDelete(String datasetName) throws IOException {
    DatasetDelete.datasetDelete(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset deleted."));
  }

  @Test
  // Use a test runner to guarantee sure the tests run sequentially.
  public void testRunner() throws IOException {
    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    String datasetName = String.format(DATASET_NAME, PROJECT_ID, REGION_ID, datasetId);

    testDatasetCreate(datasetId);
    testDatasetGet(datasetName);
    testDatasetList();
    testDatasetSetIamPolicy(datasetName);
    testDatasetGetIamPolicy(datasetName);
    testDataSetPatch(datasetName);
    testDatasetDeidentify(datasetName, datasetName + "_died");
    testDatasetDelete(datasetName);
  }
}
