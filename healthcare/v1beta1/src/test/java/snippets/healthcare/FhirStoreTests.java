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
import snippets.healthcare.fhir.FhirStoreCreate;
import snippets.healthcare.fhir.FhirStoreDelete;
import snippets.healthcare.fhir.FhirStoreExecuteBundle;
import snippets.healthcare.fhir.FhirStoreExport;
import snippets.healthcare.fhir.FhirStoreGet;
import snippets.healthcare.fhir.FhirStoreGetIamPolicy;
import snippets.healthcare.fhir.FhirStoreImport;
import snippets.healthcare.fhir.FhirStoreList;
import snippets.healthcare.fhir.FhirStorePatch;
import snippets.healthcare.fhir.FhirStoreSetIamPolicy;
import snippets.healthcare.fhir.resources.FhirResourceConditionalPatch;
import snippets.healthcare.fhir.resources.FhirResourceConditionalUpdate;
import snippets.healthcare.fhir.resources.FhirResourceCreate;
import snippets.healthcare.fhir.resources.FhirResourceDelete;
import snippets.healthcare.fhir.resources.FhirResourceDeletePurge;
import snippets.healthcare.fhir.resources.FhirResourceGet;
import snippets.healthcare.fhir.resources.FhirResourceGetHistory;
import snippets.healthcare.fhir.resources.FhirResourceGetMetadata;
import snippets.healthcare.fhir.resources.FhirResourceGetPatientEverything;
import snippets.healthcare.fhir.resources.FhirResourceListHistory;
import snippets.healthcare.fhir.resources.FhirResourcePatch;
import snippets.healthcare.fhir.resources.FhirResourceSearch;
import snippets.healthcare.fhir.resources.FhirResourceSearchPost;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FhirStoreTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION_ID = "us-central1";

  private static final String GCLOUD_BUCKET_NAME = System.getenv("GCLOUD_BUCKET_NAME");
  private static final String GCLOUD_PUBSUB_TOPIC = System.getenv("GCLOUD_PUBSUB_TOPIC");

  private static String datasetId;
  private static String datasetName;

  private static String fhirStoreId;
  private static String fhirStoreName;

  private static String fhirResourceId;
  private static String fhirResourceName;

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

    fhirStoreId = "dicom-" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirStoreName = String.format("%s/dicomStores/%s", datasetName, fhirStoreId);

    fhirResourceId = "resource-" + UUID.randomUUID().toString().replaceAll("-", "_");
    fhirResourceName = String.format("%s/fhir/%s", fhirStoreName, fhirResourceId);
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
  public void test_01_FhirStoreCreate() throws Exception {
    FhirStoreCreate.fhirStoreCreate(datasetName, fhirStoreId);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store created: "));
  }

  @Test
  public void test_02_FhirStoreGet() throws Exception {
    FhirStoreGet.fhirStoreGet(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store retrieved:"));
  }

  @Test
  public void test_02_FhirStoreGetIamPolicy() throws Exception {
    FhirStoreGetIamPolicy.fhirStoreGetIamPolicy(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store IAMPolicy retrieved:"));
  }

  @Test
  public void test_02_FhirStoreSetIamPolicy() throws Exception {
    FhirStoreSetIamPolicy.fhirStoreSetIamPolicy(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR policy has been updated:"));
  }

  @Test
  public void test_02_FhirStoreList() throws Exception {
    FhirStoreList.fhirStoreList(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("Retrieved "));
  }

  @Test
  public void test_02_FhirStorePatch() throws Exception {
    FhirStorePatch.fhirStorePatch(fhirStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("Fhir store patched:"));
  }

  @Test
  public void test_02_FhirStoreExport() throws Exception {
    FhirStoreExport.fhirStoreExport(fhirStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("Fhir store export complete."));
  }

  @Test
  public void test_02_FhirStoreImport() throws Exception {
    FhirStoreImport.fhirStoreImport(fhirStoreName, GCLOUD_PUBSUB_TOPIC);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store import complete:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_02_ExecuteFhirBundle() throws Exception {
    FhirStoreExecuteBundle.fhirStoreExecuteBundle(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR bundle executed:"));
  }

  @Test
  public void test_03_FhirStoreImport() throws Exception {
    String gcsPath =
        String.format("gcs://%s/%s", GCLOUD_BUCKET_NAME, "IM-0002-0001-JPEG-BASELINE.dcm");
    FhirStoreImport.fhirStoreImport(fhirStoreName, gcsPath);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store import complete:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceCreate() throws Exception {
    FhirResourceCreate.fhirResourceCreate(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource created:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceSearch() throws Exception {
    FhirResourceSearch.fhirResourceSearch(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceSearchPost() throws Exception {
    FhirResourceSearchPost.fhirResourceSearchPost(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceGet() throws Exception {
    FhirResourceGet.fhirResourceGet(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource retrieved:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourcePatch() throws Exception {
    FhirResourcePatch.ffirResourcePatch(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource patched:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceConditionalPatch() throws Exception {
    FhirResourceConditionalPatch.fhirResourceConditionalPatch(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource conditionally patched:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceConditionalUpdate() throws Exception {
    FhirResourceConditionalUpdate.fhirResourceConditionalUpdate(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource conditionally replaced:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_FhirResourceGetPatientEverything() throws Exception {
    FhirResourceGetPatientEverything.fhirResourceGetPatientEverything(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource search results:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_04_GetFhirResourceMetadata() throws Exception {
    FhirResourceGetMetadata.fhirResourceGetMetadata(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource metadata retrieved:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_05_FhirResourceDelete() throws Exception {
    FhirResourceDelete.fhirResourceDelete(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource deleted."));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_06_FhirResourceGetHistory() throws Exception {
    FhirResourceGetHistory.fhirResourceGetHistory(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource history retrieved:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_06_FhirResourceListHistory() throws Exception {
    FhirResourceListHistory.fhirResourceListHistory(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource history list:"));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_07_DeletePurgeFhirResource() throws Exception {
    FhirResourceDeletePurge.fhirResourceDeletePurge(fhirResourceName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR resource deleted."));
  }

  @Test
  // @Ignore // TODO: b/128844810
  public void test_08_FhirStoreDelete() throws Exception {
    FhirStoreDelete.fhirStoreDelete(fhirStoreName);

    String output = bout.toString();
    assertThat(output, containsString("FHIR store deleted."));
  }
}
