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
    assertThat(output, containsString("DICOM store created."));
  }

  @Test
  public void test_02_DicomStoreGet() throws IOException {
    DicomStoreGet.dicomeStoreGet(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store created."));
  }

  @Test
  public void test_02_DicomStoreGetIamPolicy() throws IOException {
    DicomStoreGetIamPolicy.dicomStoreGetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store IAMPolicy retrieved:"));
  }

  @Test
  public void test_02_DicomStoreSetIamPolicy() throws IOException {
    DicomStoreSetIamPolicy.dicomStoreSetIamPolicy(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM policy has been updated: "));
  }

  @Test
  public void test_02_DicomStoreList() throws IOException {
    DicomStoreList.dicomStoreList(datasetName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved \\d+ DICOM stores:"));
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
  public void test_06_DicomStoreDelete() throws IOException {
    DicomStoreDelete.deleteDicomStore(dicomStoreName);

    String output = bout.toString();
    assertThat(output, containsString("DICOM store deleted."));
  }
}
