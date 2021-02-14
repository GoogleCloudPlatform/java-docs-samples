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

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String datasetName;

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

  @Before
  public void beforeTest() throws IOException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    String parentName = String.format("projects/%s/locations/%s", PROJECT_ID, REGION_ID);
    datasetName = String.format("%s/datasets/%s", parentName, datasetId);

    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    try {
      DatasetDelete.datasetDelete(datasetName);
    } catch (GoogleJsonResponseException ex) {
      // Dataset already deleted, continue.
    }
    bout.reset();
  }

  @Test
  public void test_DatasetCreateDelete() throws IOException {
    String newName = "new-dataset";
    String newFullName =
        String.format("projects/%s/locations/%s/datasets/%s", PROJECT_ID, REGION_ID, newName);
    try {
      DatasetDelete.datasetDelete(newFullName);
    } catch (GoogleJsonResponseException gjre) {
      // Expected if new-dataset does not exist.
    }
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, newName);

    String output = bout.toString(StandardCharsets.UTF_8);
    assertThat(output, containsString("Dataset created."));
    bout.reset();

    DatasetDelete.datasetDelete(newFullName);

    output = bout.toString();
    assertThat(output, containsString("Dataset deleted."));
  }

  @Test
  public void test_DatasetGet() throws IOException {
    DatasetGet.datasetGet(datasetName);
    String output = bout.toString();
    assertThat(output, containsString("Dataset retrieved:"));
  }

  @Test
  public void test_DatasetList() throws IOException {
    DatasetList.datasetList(PROJECT_ID, REGION_ID);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved"));
  }

  @Test
  public void test_DataSetPatch() throws IOException {
    DatasetPatch.datasetPatch(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset patched:"));
  }

  @Test
  public void test_DatasetDeidentify() throws IOException {
    DatasetDeIdentify.datasetDeIdentify(datasetName, datasetName + "-died");

    String output = bout.toString();
    assertThat(output, containsString("De-identified Dataset created."));
  }

  @Test
  public void test_DatasetGetIamPolicy() throws IOException {
    DatasetGetIamPolicy.datasetGetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset IAMPolicy retrieved:"));
  }

  @Test
  public void test_DatasetSetIamPolicy() throws IOException {
    DatasetSetIamPolicy.datasetSetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Dataset policy has been updated: "));
  }
}
