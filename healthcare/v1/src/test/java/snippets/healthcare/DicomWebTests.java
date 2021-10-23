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

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
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
import snippets.healthcare.datasets.DatasetDelete;
import snippets.healthcare.dicom.DicomStoreCreate;
import snippets.healthcare.dicom.DicomWebDeleteStudy;
import snippets.healthcare.dicom.DicomWebRetrieveInstance;
import snippets.healthcare.dicom.DicomWebRetrieveRendered;
import snippets.healthcare.dicom.DicomWebRetrieveStudy;
import snippets.healthcare.dicom.DicomWebSearchForInstances;
import snippets.healthcare.dicom.DicomWebSearchStudies;
import snippets.healthcare.dicom.DicomWebStoreInstance;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DicomWebTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static String dicomStoreName;
  private static String datasetName;

  // The studyUid is not assigned by the server and is part of the metadata of dcmFile.
  private static String studyId = "2.25.330012077234033941963257891139480825153";
  private static String seriesId = "2.25.143186483950719304925806365081717734297";
  private static String instanceId = "2.25.195151962645072062560826889007364152748";
  private static String dicomWebInstancePath =
      String.format("studies/%s/series/%s/instances/%s", studyId, seriesId, instanceId);
  private static String dicomWebRenderedPath = dicomWebInstancePath + "/rendered";

  private static String instanceOutput = "instance.dcm";
  private static String renderedOutput = "image.png";
  private static String studyOutput = "study.multipart";

  private File outputFile;

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

  @BeforeClass
  public static void setUp() throws IOException {
    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    datasetName =
        String.format("projects/%s/locations/%s/datasets/%s", PROJECT_ID, REGION_ID, datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    String dicomStoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    dicomStoreName = String.format("%s/dicomStores/%s", datasetName, dicomStoreId);

    DicomStoreCreate.dicomStoreCreate(datasetName, dicomStoreId);
  }

  @AfterClass
  public static void deleteTempItems() throws IOException {
    DatasetDelete.datasetDelete(datasetName);
  }

  @Before
  public void beforeTest() throws IOException, URISyntaxException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Store DICOM instance before each test so it is always available.
    DicomWebStoreInstance.dicomWebStoreInstance(dicomStoreName, "src/test/resources/jpeg_text.dcm");

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @Test
  public void test_DicomWebStoreInstance() throws Exception {
    DicomWebStoreInstance.dicomWebStoreInstance(dicomStoreName, "src/test/resources/jpeg_text.dcm");

    String output = bout.toString();
    assertThat(output, containsString("DICOM instance stored:"));
  }

  @Test
  public void test_DicomWebSearchInstances() throws Exception {
    DicomWebSearchForInstances.dicomWebSearchForInstances(dicomStoreName);
    String output = bout.toString();
    assertThat(output, containsString("Dicom store instances found:"));
  }

  @Test
  public void test_DicomWebSearchStudies() throws Exception {
    DicomWebSearchStudies.dicomWebSearchStudies(dicomStoreName);
    String output = bout.toString();
    assertThat(output, containsString("Studies found:"));
  }

  @Test
  public void test_DicomWebRetrieveStudy() throws Exception {
    DicomWebRetrieveStudy.dicomWebRetrieveStudy(dicomStoreName, studyId);

    outputFile = new File(studyOutput);
    assertThat(outputFile.isFile()).isTrue();
    String output = bout.toString();
    assertThat(output, containsString("DICOM study written to file"));
    outputFile.delete();
  }

  @Test
  public void test_DicomWebRetrieveInstance() throws Exception {
    DicomWebRetrieveInstance.dicomWebRetrieveInstance(dicomStoreName, dicomWebInstancePath);

    outputFile = new File(instanceOutput);
    assertThat(outputFile.isFile()).isTrue();
    String output = bout.toString();
    assertThat(output, containsString("DICOM instance written to file"));
    outputFile.delete();
  }

  @Test
  public void test_DicomWebRetrieveRendered() throws Exception {
    DicomWebRetrieveRendered.dicomWebRetrieveRendered(dicomStoreName, dicomWebRenderedPath);

    outputFile = new File(renderedOutput);
    assertThat(outputFile.isFile()).isTrue();
    String output = bout.toString();
    assertThat(output, containsString("DICOM rendered PNG image written to file"));
    outputFile.delete();
  }

  @Test
  // Test order is NAME_ASCENDING, so ensure that we delete the DICOM study
  // last, otherwise it might run before DicomWebRetrieve methods
  // (see https://github.com/GoogleCloudPlatform/java-docs-samples/issues/3845).
  @SuppressWarnings("checkstyle:MethodName")
  public void z_test_DicomWebDeleteStudy() throws IOException {
    DicomWebDeleteStudy.dicomWebDeleteStudy(dicomStoreName, studyId);

    String output = bout.toString();
    assertThat(output, containsString("DICOM study deleted."));
  }
}
