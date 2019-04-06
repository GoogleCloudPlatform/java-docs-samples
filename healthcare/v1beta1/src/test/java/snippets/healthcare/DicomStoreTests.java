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
import snippets.healthcare.dicom.DicomStoreCreate;
import snippets.healthcare.dicom.DicomStoreDelete;
import snippets.healthcare.dicom.DicomStoreExport;
import snippets.healthcare.dicom.DicomStoreGet;
import snippets.healthcare.dicom.DicomStoreGetIamPolicy;
import snippets.healthcare.dicom.DicomStoreImport;
import snippets.healthcare.dicom.DicomStoreList;

@RunWith(JUnit4.class)
public class DicomStoreTests {
  private static final String DATASET_NAME = "projects/%s/locations/%s/datasets/%s";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  protected static final String GCLOUD_BUCKET_NAME = System.getenv("GCLOUD_BUCKET_NAME");
  protected static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

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

  private void testDicomStoreCreate(String datasetName, String dicomStoreId) throws IOException {
    DicomStoreCreate.dicomStoreCreate(datasetName, dicomStoreId);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store created."));
  }

  private void testDicomStoreGet(String dicomStoreName) throws IOException {
    DicomStoreGet.dicomeStoreGet(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store created."));
  }

  private void testDicomStoreList(String datasetName) throws IOException {
    DicomStoreList.dicomStoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved \\d+ DICOM stores:"));
  }

  private void testDicomStoreExport(String dicomStoreName) throws IOException {
    DicomStoreExport.dicomStoreExport(dicomStoreName, GCLOUD_BUCKET_NAME);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store export complete."));
  }

  private void dicomStoreGetIamPolicy(String datasetName) throws IOException {
    DicomStoreGetIamPolicy.dicomStoreGetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store IAMPolicy retrieved:"));
  }

  private void testDicomStoreImport(String dicomStoreName) throws IOException {
    String gcsPath =
        String.format("gcs://%s/%s", GCLOUD_BUCKET_NAME, "IM-0002-0001-JPEG-BASELINE.dcm");
    DicomStoreImport.dicomStoreImport(dicomStoreName, gcsPath);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store import complete."));
  }

  private void testDicomStoreDelete(String dicomStoreName) throws IOException {
    DicomStoreDelete.deleteDicomStore(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store deleted."));
  }

  @Test
  // Use a test runner to guarantee sure the tests run sequentially.
  public void testRunner() throws IOException {
    String datasetId = "dataset-" + UUID.randomUUID().toString().replaceAll("-", "_");
    String datasetName = String.format(DATASET_NAME, PROJECT_ID, REGION_ID, datasetId);
    DatasetCreate.datasetCreate(PROJECT_ID, REGION_ID, datasetId);

    String dicomStoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    String dicomStoreName = String.format("%s/dicomStores/%s", datasetName, dicomStoreId);

    testDicomStoreCreate(datasetName, dicomStoreId);
    testDicomStoreGet(dicomStoreName);
    testDicomStoreList(dicomStoreName);
    testDicomStoreExport(dicomStoreName);
    testDicomStoreImport(dicomStoreName);
    dicomStoreGetIamPolicy(dicomStoreName);
    testDicomStoreDelete(dicomStoreName);
  }
}
